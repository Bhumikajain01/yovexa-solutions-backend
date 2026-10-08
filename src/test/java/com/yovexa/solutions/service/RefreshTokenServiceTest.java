package com.yovexa.solutions.service;

import com.yovexa.solutions.exception.UnauthorizedException;
import com.yovexa.solutions.model.Admin;
import com.yovexa.solutions.model.RefreshToken;
import com.yovexa.solutions.repository.AdminRepository;
import com.yovexa.solutions.repository.RefreshTokenRepository;
import com.yovexa.solutions.security.JwtService;
import com.yovexa.solutions.util.TokenHashUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache adminCache;

    private RefreshTokenService refreshTokenService;
    private Admin testAdmin;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(
                refreshTokenRepository,
                adminRepository,
                jwtService,
                cacheManager
        );

        ReflectionTestUtils.setField(refreshTokenService, "refreshExpirationDays", 7);
        ReflectionTestUtils.setField(refreshTokenService, "gracePeriodSeconds", 15);

        testAdmin = Admin.builder()
                .id("admin-123")
                .name("Super Admin")
                .email("admin@yovexa.com")
                .role("ADMIN")
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("createRefreshToken creates a new token family and saves hashed token")
    void testCreateRefreshToken() {
        String rawToken = refreshTokenService.createRefreshToken(testAdmin, "127.0.0.1", "Mozilla/5.0");

        assertThat(rawToken).isNotBlank();
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());

        RefreshToken saved = captor.getValue();
        assertThat(saved.getAdminId()).isEqualTo("admin-123");
        assertThat(saved.getAdminEmail()).isEqualTo("admin@yovexa.com");
        assertThat(saved.getTokenHash()).isEqualTo(TokenHashUtil.hashToken(rawToken));
        assertThat(saved.getFamilyId()).isNotBlank();
        assertThat(saved.isUsed()).isFalse();
        assertThat(saved.isRevoked()).isFalse();
    }

    @Test
    @DisplayName("rotate rotates unused token, issues child token in same family, and marks parent used")
    void testRotateSuccess() {
        String rawToken = "initial-raw-token";
        String tokenHash = TokenHashUtil.hashToken(rawToken);
        String familyId = "family-uuid-1";

        RefreshToken storedToken = RefreshToken.builder()
                .id("tok-1")
                .tokenHash(tokenHash)
                .familyId(familyId)
                .adminId("admin-123")
                .adminEmail("admin@yovexa.com")
                .isUsed(false)
                .isRevoked(false)
                .createdAt(Instant.now().minusSeconds(100))
                .expiresAt(Instant.now().plus(Duration.ofDays(7)))
                .build();

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(storedToken));
        when(adminRepository.findById("admin-123")).thenReturn(Optional.of(testAdmin));
        when(jwtService.generateToken(testAdmin)).thenReturn("mocked-new-access-token");
        when(jwtService.getExpirationInSeconds()).thenReturn(900L);

        RefreshTokenService.RotationResult result = refreshTokenService.rotate(rawToken, "127.0.0.1", "Mozilla/5.0");

        assertThat(result.newAccessToken()).isEqualTo("mocked-new-access-token");
        assertThat(result.newRefreshToken()).isNotBlank();
        assertThat(result.expiresIn()).isEqualTo(900L);

        // Stored token was updated to used
        assertThat(storedToken.isUsed()).isTrue();
        assertThat(storedToken.getUsedAt()).isNotNull();
        assertThat(storedToken.getReplacedByTokenHash()).isNotNull();

        // 2 saves: updated stored token + new child token
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("rotate detects token reuse outside grace period and revokes entire family")
    void testRotateReuseDetectionTriggersFamilyRevocation() {
        String rawToken = "stolen-already-used-token";
        String tokenHash = TokenHashUtil.hashToken(rawToken);
        String familyId = "family-uuid-compromised";

        RefreshToken alreadyUsedToken = RefreshToken.builder()
                .id("tok-old")
                .tokenHash(tokenHash)
                .familyId(familyId)
                .adminId("admin-123")
                .adminEmail("admin@yovexa.com")
                .isUsed(true)
                .isRevoked(false)
                .createdAt(Instant.now().minusSeconds(500))
                .usedAt(Instant.now().minusSeconds(60)) // 60s ago -> outside 15s grace period!
                .replacedByTokenHash("some-other-hash")
                .expiresAt(Instant.now().plus(Duration.ofDays(7)))
                .build();

        RefreshToken siblingToken = RefreshToken.builder()
                .id("tok-sibling")
                .familyId(familyId)
                .isRevoked(false)
                .build();

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(alreadyUsedToken));
        when(refreshTokenRepository.findAllByFamilyId(familyId)).thenReturn(List.of(alreadyUsedToken, siblingToken));
        when(cacheManager.getCache(anyString())).thenReturn(adminCache);

        assertThatThrownBy(() -> refreshTokenService.rotate(rawToken, "198.51.100.1", "Malicious-Agent"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Security breach detected");

        // Verify entire family is revoked
        assertThat(alreadyUsedToken.isRevoked()).isTrue();
        assertThat(alreadyUsedToken.getRevokedReason()).isEqualTo("REUSE_DETECTED");
        assertThat(siblingToken.isRevoked()).isTrue();
        assertThat(siblingToken.getRevokedReason()).isEqualTo("REUSE_DETECTED");

        verify(refreshTokenRepository).saveAll(List.of(alreadyUsedToken, siblingToken));
        verify(adminCache).clear(); // Cache eviction triggered
    }

    @Test
    @DisplayName("rotate tolerates concurrent requests within grace period without triggering false reuse alarm")
    void testRotateGracePeriodTolerance() {
        String rawToken = "concurrent-parallel-token";
        String tokenHash = TokenHashUtil.hashToken(rawToken);
        String familyId = "family-parallel";

        RefreshToken recentlyUsedToken = RefreshToken.builder()
                .id("tok-concurrent")
                .tokenHash(tokenHash)
                .familyId(familyId)
                .adminId("admin-123")
                .adminEmail("admin@yovexa.com")
                .isUsed(true)
                .isRevoked(false)
                .createdAt(Instant.now().minusSeconds(10))
                .usedAt(Instant.now().minusSeconds(2)) // 2s ago -> within 15s grace period!
                .replacedByTokenHash("child-hash-123")
                .expiresAt(Instant.now().plus(Duration.ofDays(7)))
                .build();

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(recentlyUsedToken));
        when(adminRepository.findById("admin-123")).thenReturn(Optional.of(testAdmin));
        when(jwtService.generateToken(testAdmin)).thenReturn("grace-window-access-token");
        when(jwtService.getExpirationInSeconds()).thenReturn(900L);

        RefreshTokenService.RotationResult result = refreshTokenService.rotate(rawToken, "127.0.0.1", "Mozilla/5.0");

        assertThat(result.newAccessToken()).isEqualTo("grace-window-access-token");
        assertThat(result.newRefreshToken()).isNull(); // No new rotation needed during grace window
        assertThat(recentlyUsedToken.isRevoked()).isFalse(); // NOT revoked!
    }

    @Test
    @DisplayName("revokeAllForAdmin revokes all active tokens on password change")
    void testRevokeAllForAdmin() {
        RefreshToken token1 = RefreshToken.builder().id("1").adminId("admin-123").isRevoked(false).build();
        RefreshToken token2 = RefreshToken.builder().id("2").adminId("admin-123").isRevoked(false).build();

        when(refreshTokenRepository.findAllByAdminIdAndIsRevokedFalse("admin-123"))
                .thenReturn(List.of(token1, token2));

        refreshTokenService.revokeAllForAdmin("admin-123", "PASSWORD_CHANGED");

        assertThat(token1.isRevoked()).isTrue();
        assertThat(token1.getRevokedReason()).isEqualTo("PASSWORD_CHANGED");
        assertThat(token2.isRevoked()).isTrue();
        assertThat(token2.getRevokedReason()).isEqualTo("PASSWORD_CHANGED");

        verify(refreshTokenRepository).saveAll(List.of(token1, token2));
    }
}
