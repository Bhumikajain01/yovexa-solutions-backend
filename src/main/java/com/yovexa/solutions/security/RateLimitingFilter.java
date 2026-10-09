package com.yovexa.solutions.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yovexa.solutions.dto.common.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory sliding-window rate limiting filter.
 * Protects authentication, password changes, contact submissions, and general APIs
 * against brute-force and Denial-of-Service attacks without adding external dependencies.
 */
@Slf4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Map: key = (ip + ":" + endpointCategory) -> Counter
    private final Map<String, WindowCounter> requestCounts = new ConcurrentHashMap<>();

    // Eviction tracker
    private volatile long lastEvictionTime = System.currentTimeMillis();
    private static final long EVICTION_INTERVAL_MS = 300_000L; // 5 minutes

    private static class WindowCounter {
        final long windowStart;
        final AtomicInteger count;

        WindowCounter(long windowStart) {
            this.windowStart = windowStart;
            this.count = new AtomicInteger(1);
        }
    }

    private static class RateRule {
        final int maxRequests;
        final long windowMs;

        RateRule(int maxRequests, long windowMs) {
            this.maxRequests = maxRequests;
            this.windowMs = windowMs;
        }
    }

    // Default rate limits per 1-minute window
    private static final RateRule RULE_LOGIN = new RateRule(10, 60_000L);
    private static final RateRule RULE_REGISTER = new RateRule(5, 60_000L);
    private static final RateRule RULE_PASSWORD = new RateRule(5, 60_000L);
    private static final RateRule RULE_INQUIRY = new RateRule(10, 60_000L);
    private static final RateRule RULE_GENERAL = new RateRule(150, 60_000L);

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Skip rate limiting for CORS preflight OPTIONS requests and documentation
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        String category = resolveCategory(path, request.getMethod());
        RateRule rule = resolveRule(category);

        String clientIp = extractClientIp(request);
        String key = clientIp + ":" + category;

        long now = System.currentTimeMillis();
        triggerEvictionIfNecessary(now);

        WindowCounter counter = requestCounts.compute(key, (k, existing) -> {
            if (existing == null || (now - existing.windowStart) > rule.windowMs) {
                return new WindowCounter(now);
            }
            existing.count.incrementAndGet();
            return existing;
        });

        if (counter != null && counter.count.get() > rule.maxRequests) {
            log.warn("Rate limit exceeded for IP: {} on category: {} ({}/{} req)",
                    clientIp, category, counter.count.get(), rule.maxRequests);

            long retryAfterSec = Math.max(1, (rule.windowMs - (now - counter.windowStart)) / 1000);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", String.valueOf(retryAfterSec));

            ApiResponse<Object> apiResponse = ApiResponse.error(
                    "Too many requests. Please slow down and try again.",
                    Collections.singletonList("Rate limit exceeded. Retry in " + retryAfterSec + " seconds.")
            );
            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveCategory(String path, String method) {
        if ("/api/auth/login".equalsIgnoreCase(path)) {
            return "LOGIN";
        }
        if ("/api/auth/register".equalsIgnoreCase(path)) {
            return "REGISTER";
        }
        if ("/api/admin/profile/password".equalsIgnoreCase(path)) {
            return "PASSWORD";
        }
        if ("/api/inquiries".equalsIgnoreCase(path) && "POST".equalsIgnoreCase(method)) {
            return "INQUIRY";
        }
        return "GENERAL";
    }

    private RateRule resolveRule(String category) {
        return switch (category) {
            case "LOGIN" -> RULE_LOGIN;
            case "REGISTER" -> RULE_REGISTER;
            case "PASSWORD" -> RULE_PASSWORD;
            case "INQUIRY" -> RULE_INQUIRY;
            default -> RULE_GENERAL;
        };
    }

    private String extractClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xfHeader)) {
            return xfHeader.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private void triggerEvictionIfNecessary(long now) {
        if (now - lastEvictionTime > EVICTION_INTERVAL_MS) {
            synchronized (this) {
                if (now - lastEvictionTime > EVICTION_INTERVAL_MS) {
                    lastEvictionTime = now;
                    requestCounts.entrySet().removeIf(entry -> (now - entry.getValue().windowStart) > 120_000L);
                }
            }
        }
    }
}
