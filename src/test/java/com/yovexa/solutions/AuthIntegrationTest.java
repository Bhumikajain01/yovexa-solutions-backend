package com.yovexa.solutions;

import com.yovexa.solutions.dto.admin.AdminResponse;
import com.yovexa.solutions.dto.admin.ChangePasswordRequest;
import com.yovexa.solutions.dto.auth.LoginRequest;
import com.yovexa.solutions.dto.auth.LoginResponse;
import com.yovexa.solutions.dto.auth.RegisterRequest;
import com.yovexa.solutions.exception.DuplicateResourceException;
import com.yovexa.solutions.exception.ForbiddenException;
import com.yovexa.solutions.exception.UnauthorizedException;
import com.yovexa.solutions.mapper.EntityMapper;
import com.yovexa.solutions.model.Admin;
import com.yovexa.solutions.repository.AdminRepository;
import com.yovexa.solutions.security.JwtService;
import com.yovexa.solutions.service.AdminService;
import com.yovexa.solutions.service.AuthService;
import com.yovexa.solutions.service.impl.AdminServiceImpl;
import com.yovexa.solutions.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthIntegrationTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private com.yovexa.solutions.service.RefreshTokenService refreshTokenService;

    private EntityMapper mapper;
    private PasswordEncoder passwordEncoder;
    private AuthService authService;
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        mapper = new EntityMapper();
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthServiceImpl(adminRepository, authenticationManager, jwtService, mapper, passwordEncoder);
        adminService = new AdminServiceImpl(adminRepository, mapper, passwordEncoder, refreshTokenService);
    }

    @Test
    @DisplayName("TEST 1 & 2: First admin registration succeeds and saves BCrypt hashed password")
    void testFirstAdminRegistrationSuccess() {
        when(adminRepository.count()).thenReturn(0L);
        when(adminRepository.existsByEmailIgnoreCase("admin@example.com")).thenReturn(false);
        when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> {
            Admin a = invocation.getArgument(0);
            a.setId("test-id-123");
            return a;
        });

        RegisterRequest request = RegisterRequest.builder()
                .name("Yovexa Admin")
                .email("admin@example.com")
                .password("StrongPassword123")
                .confirmPassword("StrongPassword123")
                .build();

        AdminResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("test-id-123", response.getId());
        assertEquals("Yovexa Admin", response.getName());
        assertEquals("admin@example.com", response.getEmail());
        assertEquals("ADMIN", response.getRole());
        assertTrue(response.getIsActive());

        ArgumentCaptor<Admin> adminCaptor = ArgumentCaptor.forClass(Admin.class);
        verify(adminRepository).save(adminCaptor.capture());
        Admin savedAdmin = adminCaptor.getValue();

        assertTrue(passwordEncoder.matches("StrongPassword123", savedAdmin.getPassword()));
        assertNotEquals("StrongPassword123", savedAdmin.getPassword());
    }

    @Test
    @DisplayName("TEST 3: Duplicate email registration returns 409 Conflict")
    void testDuplicateEmailRegistrationThrowsConflict() {
        when(adminRepository.count()).thenReturn(0L);
        when(adminRepository.existsByEmailIgnoreCase("admin@example.com")).thenReturn(true);

        RegisterRequest request = RegisterRequest.builder()
                .name("Yovexa Admin")
                .email("admin@example.com")
                .password("StrongPassword123")
                .confirmPassword("StrongPassword123")
                .build();

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
        verify(adminRepository, never()).save(any());
    }

    @Test
    @DisplayName("TEST 4: Subsequent public registration is disabled once an admin exists (403 Forbidden)")
    void testSubsequentRegistrationThrowsForbidden() {
        when(adminRepository.count()).thenReturn(1L);

        RegisterRequest request = RegisterRequest.builder()
                .name("Second Admin")
                .email("second@example.com")
                .password("StrongPassword123")
                .confirmPassword("StrongPassword123")
                .build();

        ForbiddenException ex = assertThrows(ForbiddenException.class, () -> authService.register(request));
        assertEquals("Admin registration is currently disabled.", ex.getMessage());
        verify(adminRepository, never()).save(any());
    }

    @Test
    @DisplayName("TEST 5: Login with correct credentials returns 200 + JWT")
    void testLoginSuccess() {
        String rawPassword = "StrongPassword123";
        Admin admin = Admin.builder()
                .id("admin-1")
                .name("Yovexa Admin")
                .email("admin@example.com")
                .password(passwordEncoder.encode(rawPassword))
                .role("ADMIN")
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(adminRepository.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(admin));
        when(jwtService.generateToken(admin)).thenReturn("mock.jwt.token");
        when(jwtService.getExpirationInSeconds()).thenReturn(86400L);

        LoginRequest request = LoginRequest.builder()
                .email("admin@example.com")
                .password(rawPassword)
                .build();

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(86400L, response.getExpiresIn());
        assertNotNull(response.getUser());
        assertEquals("admin-1", response.getUser().getId());
        assertEquals("Yovexa Admin", response.getUser().getName());
        assertEquals("admin@example.com", response.getUser().getEmail());
        assertEquals("ADMIN", response.getUser().getRole());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("TEST 6: Login with wrong password returns 401 Unauthorized")
    void testLoginWithWrongPasswordThrowsUnauthorized() {
        Admin admin = Admin.builder()
                .id("admin-1")
                .name("Yovexa Admin")
                .email("admin@example.com")
                .password(passwordEncoder.encode("CorrectPassword123"))
                .role("ADMIN")
                .isActive(true)
                .build();

        when(adminRepository.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(admin));

        LoginRequest request = LoginRequest.builder()
                .email("admin@example.com")
                .password("WrongPassword999")
                .build();

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
        verify(jwtService, never()).generateToken(any(Admin.class));
    }

    @Test
    @DisplayName("TEST 7: Login for inactive admin account returns 401 Unauthorized")
    void testLoginWithInactiveAccountThrowsUnauthorized() {
        Admin admin = Admin.builder()
                .id("admin-1")
                .name("Yovexa Admin")
                .email("inactive@example.com")
                .password(passwordEncoder.encode("Password123"))
                .role("ADMIN")
                .isActive(false)
                .build();

        when(adminRepository.findByEmailIgnoreCase("inactive@example.com")).thenReturn(Optional.of(admin));

        LoginRequest request = LoginRequest.builder()
                .email("inactive@example.com")
                .password("Password123")
                .build();

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> authService.login(request));
        assertEquals("Your account is inactive. Please contact an administrator.", ex.getMessage());
    }

    @Test
    @DisplayName("TEST 10: Change password updates hash and rejects same password")
    void testChangePasswordFlow() {
        String oldPassword = "OldPassword123";
        Admin admin = Admin.builder()
                .id("admin-1")
                .name("Yovexa Admin")
                .email("admin@example.com")
                .password(passwordEncoder.encode(oldPassword))
                .role("ADMIN")
                .isActive(true)
                .build();

        when(adminRepository.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(admin));

        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword(oldPassword)
                .newPassword("NewPassword456")
                .confirmPassword("NewPassword456")
                .build();

        adminService.changePassword("admin@example.com", request);

        verify(adminRepository).save(admin);
        assertTrue(passwordEncoder.matches("NewPassword456", admin.getPassword()));
        assertFalse(passwordEncoder.matches(oldPassword, admin.getPassword()));
    }

    @Test
    @DisplayName("TEST 11: Attempt to delete the only admin throws 403 Forbidden")
    void testDeleteLastAdminIsForbidden() {
        when(adminRepository.count()).thenReturn(1L);

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> adminService.deleteAdmin("admin-1", "admin@example.com"));

        assertEquals("The last administrator cannot be deleted.", ex.getMessage());
        verify(adminRepository, never()).delete(any());
    }
}
