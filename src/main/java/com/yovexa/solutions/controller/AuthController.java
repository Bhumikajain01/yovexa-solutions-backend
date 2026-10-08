package com.yovexa.solutions.controller;

import com.yovexa.solutions.dto.admin.AdminResponse;
import com.yovexa.solutions.dto.auth.LoginRequest;
import com.yovexa.solutions.dto.auth.LoginResponse;
import com.yovexa.solutions.dto.auth.RegisterRequest;
import com.yovexa.solutions.dto.auth.TokenRefreshRequest;
import com.yovexa.solutions.dto.auth.TokenRefreshResponse;
import com.yovexa.solutions.dto.common.ApiResponse;
import com.yovexa.solutions.exception.UnauthorizedException;
import com.yovexa.solutions.model.Admin;
import com.yovexa.solutions.repository.AdminRepository;
import com.yovexa.solutions.service.AuthService;
import com.yovexa.solutions.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Admin Registration, Login, Token Rotation, and Session Endpoints")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final AdminRepository adminRepository;

    @Value("${jwt.refresh.expiration-days:7}")
    private int refreshExpirationDays;

    @Value("${jwt.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${jwt.cookie.same-site:Lax}")
    private String cookieSameSite;

    @PostMapping("/register")
    @Operation(
        summary = "Register initial admin",
        description = "Creates the initial administrator account. Allowed only when zero admins exist in the database; returns 409 Conflict once an administrator is already registered."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Admin account registered successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed - invalid input parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Registration conflict - an administrator account already exists")
    })
    public ResponseEntity<ApiResponse<AdminResponse>> register(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Admin registration details (name, email, password, confirmPassword)", required = true)
            RegisterRequest request) {
        AdminResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Admin account created successfully.", response));
    }

    @PostMapping("/login")
    @Operation(
        summary = "Admin login",
        description = "Authenticates administrator email and password, issuing a short-lived JWT access token and setting a secure HttpOnly refresh token cookie."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Authentication successful - JWT token issued and refresh token cookie set"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed - invalid email or password format"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - invalid credentials or deactivated account")
    })
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Admin login credentials (email and password)", required = true)
            LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        LoginResponse response = authService.login(request);
        Admin admin = adminRepository.findByEmailIgnoreCase(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Admin account not found."));

        String clientIp = extractClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        // Generate and persist refresh token lineage
        String rawRefreshToken = refreshTokenService.createRefreshToken(admin, clientIp, userAgent);

        // Populate DTO for clients not supporting cookies
        response.setRefreshToken(rawRefreshToken);

        // Set secure HttpOnly cookie for browser clients
        setRefreshTokenCookie(httpResponse, rawRefreshToken, Duration.ofDays(refreshExpirationDays), httpRequest);

        return ResponseEntity.ok(ApiResponse.success("Login successful.", response));
    }

    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh JWT access token",
        description = "Performs Refresh Token Rotation (RTR) with reuse detection. Accepts the refresh token via HttpOnly cookie or JSON request body."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - invalid, expired, or reused token (triggers session termination)")
    })
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refresh(
            @CookieValue(name = "refreshToken", required = false) String cookieRefreshToken,
            @RequestBody(required = false) TokenRefreshRequest requestBody,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        String rawRefreshToken = cookieRefreshToken;
        if ((!StringUtils.hasText(rawRefreshToken)) && requestBody != null) {
            rawRefreshToken = requestBody.getRefreshToken();
        }

        if (!StringUtils.hasText(rawRefreshToken)) {
            throw new UnauthorizedException("Refresh token is required via cookie or body.");
        }

        String clientIp = extractClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        RefreshTokenService.RotationResult result = refreshTokenService.rotate(rawRefreshToken, clientIp, userAgent);

        // If a new child refresh token was minted, update the HttpOnly cookie
        if (result.newRefreshToken() != null) {
            setRefreshTokenCookie(httpResponse, result.newRefreshToken(), Duration.ofDays(refreshExpirationDays), httpRequest);
        }

        TokenRefreshResponse response = TokenRefreshResponse.builder()
                .token(result.newAccessToken())
                .refreshToken(result.newRefreshToken() != null ? result.newRefreshToken() : rawRefreshToken)
                .tokenType("Bearer")
                .expiresIn(result.expiresIn())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully.", response));
    }

    @PostMapping("/logout")
    @Operation(
        summary = "Admin logout",
        description = "Revokes the active refresh token and clears the authentication cookie."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logout successful")
    })
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(name = "refreshToken", required = false) String cookieRefreshToken,
            @RequestBody(required = false) TokenRefreshRequest requestBody,
            HttpServletResponse httpResponse) {

        String rawRefreshToken = cookieRefreshToken;
        if ((!StringUtils.hasText(rawRefreshToken)) && requestBody != null) {
            rawRefreshToken = requestBody.getRefreshToken();
        }

        if (StringUtils.hasText(rawRefreshToken)) {
            refreshTokenService.logout(rawRefreshToken);
        }

        authService.logout();
        clearRefreshTokenCookie(httpResponse);

        return ResponseEntity.ok(ApiResponse.successMessage("Logout successful"));
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String token, Duration duration, HttpServletRequest request) {
        boolean isSecure = cookieSecure || request.isSecure() || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"));
        ResponseCookie cookie = ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(isSecure)
                .sameSite(cookieSameSite)
                .path("/api/auth")
                .maxAge(duration)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/api/auth")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
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
}
