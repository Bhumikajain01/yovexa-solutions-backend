package com.yovexa.solutions.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Configuration
public class CorsConfig implements WebMvcConfigurer {

        @Value("${frontend.allowed-origins:https://yovexa-solutions1.vercel.app,https://yovexa-solutions-backend1.vercel.app}")
        private String allowedOrigins;

        private static final List<String> ALLOWED_METHODS = Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"
        );

        private static final List<String> ALLOWED_HEADERS = Arrays.asList(
                "Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With",
                "Access-Control-Request-Method", "Access-Control-Request-Headers"
        );

        private static final List<String> EXPOSED_HEADERS = Arrays.asList(
                "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials",
                "Authorization", "Set-Cookie"
        );

        private List<String> getAllowedOriginsList() {
                Set<String> origins = new LinkedHashSet<>(Arrays.asList(
                                "https://yovexa-solutions1.vercel.app",
                                "https://yovexa-solutions-backend1.vercel.app",
                                "http://localhost:5173",
                                "http://localhost:5174",
                                "http://localhost:8080",
                                "http://localhost:8081",
                                "http://localhost:3000"));

                if (allowedOrigins != null && !allowedOrigins.isBlank()) {
                        Arrays.stream(allowedOrigins.split(","))
                                        .map(String::trim)
                                        .map(s -> s.endsWith("/") ? s.substring(0, s.length() - 1) : s)
                                        .filter(s -> !s.isBlank())
                                        .forEach(origins::add);
                }
                return new ArrayList<>(origins);
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                List<String> origins = getAllowedOriginsList();
                log.info("Configuring CORS with explicit allowed origins: {}", origins);

                configuration.setAllowedOrigins(origins);
                configuration.setAllowedOriginPatterns(Arrays.asList(
                                "http://localhost:*",
                                "http://127.0.0.1:*"));
                configuration.setAllowedMethods(ALLOWED_METHODS);
                configuration.setAllowedHeaders(ALLOWED_HEADERS);
                configuration.setExposedHeaders(EXPOSED_HEADERS);
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        @Override
        public void addCorsMappings(@NonNull CorsRegistry registry) {
                List<String> origins = getAllowedOriginsList();
                registry.addMapping("/**")
                                .allowedOrigins(origins.toArray(new String[0]))
                                .allowedOriginPatterns(
                                                "http://localhost:*",
                                                "http://127.0.0.1:*")
                                .allowedMethods(ALLOWED_METHODS.toArray(new String[0]))
                                .allowedHeaders(ALLOWED_HEADERS.toArray(new String[0]))
                                .exposedHeaders(EXPOSED_HEADERS.toArray(new String[0]))
                                .allowCredentials(true)
                                .maxAge(3600);
        }
}
