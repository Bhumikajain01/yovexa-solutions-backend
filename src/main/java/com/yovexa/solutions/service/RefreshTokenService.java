package com.yovexa.solutions.service;

import com.yovexa.solutions.config.RedisConfig;
import com.yovexa.solutions.exception.UnauthorizedException;
import com.yovexa.solutions.model.Admin;
import com.yovexa.solutions.model.RefreshToken;
import com.yovexa.solutions.repository.AdminRepository;
import com.yovexa.solutions.repository.RefreshTokenRepository;
import com.yovexa.solutions.security.JwtService;
import com.yovexa.solutions.util.TokenHashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service managing Refresh Token Rotation (RTR) and Token Reuse Detection.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AdminRepository adminRepository;
    private final JwtService jwtService;
    private final CacheManager cacheManager;

    @Value("${jwt.refresh.expiration-days:7}")
    private int refreshExpirationDays;

    @Value("${jwt.refresh.grace-period-seconds:15}")
    private int gracePeriodSeconds;

    public record RotationResult(String newAccessToken, String newRefreshToken, long expiresIn) {}

    /**
     * Creates an initial refresh token upon successful credentials login.
     * Starts a new token family.
     */
    public String createRefreshToken(Admin admin, String ipAddress, String userAgent) {
        String rawToken = TokenHashUtil.generateSecureRandomToken();
        String tokenHash = TokenHashUtil.hashToken(rawToken);
        String familyId = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(tokenHash)
                .familyId(familyId)
                .adminId(admin.getId())
                .adminEmail(admin.getEmail())
                .isUsed(false)
                .isRevoked(false)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(Duration.ofDays(refreshExpirationDays)))
                .createdByIp(ipAddress)
                .userAgent(userAgent)
                .build();

        refreshTokenRepository.save(refreshToken);
        log.info("Created initial refresh token family [{}] for admin [{}] from IP [{}]",
                familyId, admin.getEmail(), ipAddress);
        return rawToken;
    }

    /**
     * Rotates a refresh token with automatic reuse detection and concurrency grace period.
     *
     * @param rawRefreshToken Presented plain text refresh token
     * @param ipAddress       Client IP address for telemetry and audit
     * @param userAgent       Client User-Agent for telemetry
     * @return RotationResult containing new access token and optional newly rotated refresh token
     */
    public RotationResult rotate(String rawRefreshToken, String ipAddress, String userAgent) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new UnauthorizedException("Refresh token is required.");
        }

        String presentedHash = TokenHashUtil.hashToken(rawRefreshToken);
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(presentedHash)
                .orElseThrow(() -> {
                    log.warn("SECURITY ALERT: Unknown refresh token presented from IP: {}", ipAddress);
                    return new UnauthorizedException("Invalid refresh token.");
                });

        // 1. Explicitly revoked check
        if (storedToken.isRevoked()) {
            log.error("SECURITY ALERT: Revoked refresh token presented. Family: {}, Admin: {}, IP: {}",
                    storedToken.getFamilyId(), storedToken.getAdminEmail(), ipAddress);
            revokeFamily(storedToken.getFamilyId(), "REVOKED_TOKEN_PRESENTED");
            evictAdminCache();
            throw new UnauthorizedException("Session has been revoked. Please log in again.");
        }

        // 2. Expiration check
        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            storedToken.setRevoked(true);
            storedToken.setRevokedReason("EXPIRED");
            refreshTokenRepository.save(storedToken);
            throw new UnauthorizedException("Refresh token has expired. Please log in again.");
        }

        // 3. REUSE DETECTION: Token was already marked as used!
        if (storedToken.isUsed()) {
            Instant graceCutoff = storedToken.getUsedAt() != null
                    ? storedToken.getUsedAt().plusSeconds(gracePeriodSeconds)
                    : storedToken.getCreatedAt().plusSeconds(gracePeriodSeconds);

            // Concurrency Grace Window: Allow in-flight parallel browser requests to get a valid access token
            if (Instant.now().isBefore(graceCutoff) && storedToken.getReplacedByTokenHash() != null) {
                log.info("Concurrent refresh request within grace period for Family [{}] from IP [{}]. Returning new access token.",
                        storedToken.getFamilyId(), ipAddress);

                Admin admin = adminRepository.findById(storedToken.getAdminId())
                        .orElseThrow(() -> new UnauthorizedException("Admin account not found."));

                String accessToken = jwtService.generateToken(admin);
                // Return new access token without rotating cookie again during grace period
                return new RotationResult(accessToken, null, jwtService.getExpirationInSeconds());
            }

            // Outside grace window: Potential Replay Attack or Token Theft!
            log.error("CRITICAL SECURITY COMPROMISE: Refresh token reuse detected! Family: [{}], Admin: [{}], IP: [{}], UserAgent: [{}]",
                    storedToken.getFamilyId(), storedToken.getAdminEmail(), ipAddress, userAgent);

            // Invalidate the entire token family immediately
            revokeFamily(storedToken.getFamilyId(), "REUSE_DETECTED");
            evictAdminCache();

            throw new UnauthorizedException("Security breach detected. All sessions terminated. Please re-authenticate.");
        }

        // 4. Normal Legitimate Rotation
        Admin admin = adminRepository.findById(storedToken.getAdminId())
                .orElseThrow(() -> new UnauthorizedException("Admin not found."));

        if (Boolean.FALSE.equals(admin.getIsActive())) {
            throw new UnauthorizedException("Admin account is deactivated.");
        }

        // Issue new child refresh token within the same family
        String newRawToken = TokenHashUtil.generateSecureRandomToken();
        String newHash = TokenHashUtil.hashToken(newRawToken);

        RefreshToken childToken = RefreshToken.builder()
                .tokenHash(newHash)
                .familyId(storedToken.getFamilyId()) // Same family lineage
                .adminId(admin.getId())
                .adminEmail(admin.getEmail())
                .isUsed(false)
                .isRevoked(false)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(Duration.ofDays(refreshExpirationDays)))
                .createdByIp(ipAddress)
                .userAgent(userAgent)
                .build();

        // Mark current token as consumed
        storedToken.setUsed(true);
        storedToken.setUsedAt(Instant.now());
        storedToken.setReplacedByTokenHash(newHash);

        refreshTokenRepository.save(storedToken);
        refreshTokenRepository.save(childToken);

        String newAccessToken = jwtService.generateToken(admin);
        log.debug("Successfully rotated refresh token for admin [{}] in Family [{}]",
                admin.getEmail(), storedToken.getFamilyId());

        return new RotationResult(newAccessToken, newRawToken, jwtService.getExpirationInSeconds());
    }

    /**
     * Revokes all tokens in the specified family (lineage).
     */
    public void revokeFamily(String familyId, String reason) {
        List<RefreshToken> tokens = refreshTokenRepository.findAllByFamilyId(familyId);
        for (RefreshToken token : tokens) {
            token.setRevoked(true);
            token.setRevokedReason(reason);
        }
        refreshTokenRepository.saveAll(tokens);
        log.warn("Revoked {} tokens for Family ID [{}] with Reason [{}]", tokens.size(), familyId, reason);
    }

    /**
     * Revokes all active refresh tokens for an admin (e.g. on password change, deactivation, or compromise).
     */
    public void revokeAllForAdmin(String adminId, String reason) {
        List<RefreshToken> activeTokens = refreshTokenRepository.findAllByAdminIdAndIsRevokedFalse(adminId);
        for (RefreshToken token : activeTokens) {
            token.setRevoked(true);
            token.setRevokedReason(reason);
        }
        refreshTokenRepository.saveAll(activeTokens);
        log.warn("Revoked {} active refresh tokens for Admin ID [{}] with Reason [{}]",
                activeTokens.size(), adminId, reason);
    }

    /**
     * Revokes a single refresh token during regular session logout.
     */
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            String hash = TokenHashUtil.hashToken(rawRefreshToken);
            refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> {
                token.setRevoked(true);
                token.setRevokedReason("LOGOUT");
                refreshTokenRepository.save(token);
                log.info("Revoked refresh token on logout for admin [{}] in Family [{}]",
                        token.getAdminEmail(), token.getFamilyId());
            });
        }
    }

    private void evictAdminCache() {
        try {
            if (cacheManager != null) {
                Cache cache = cacheManager.getCache(RedisConfig.ADMINS_CACHE);
                if (cache != null) {
                    cache.clear();
                    log.info("Evicted admin cache due to security revocation.");
                }
            }
        } catch (Exception e) {
            log.warn("Failed to evict admin cache during revocation: {}", e.getMessage());
        }
    }
}
