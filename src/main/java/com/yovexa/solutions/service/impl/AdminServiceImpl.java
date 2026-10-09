package com.yovexa.solutions.service.impl;

import com.yovexa.solutions.dto.admin.*;
import com.yovexa.solutions.exception.DuplicateResourceException;
import com.yovexa.solutions.exception.ForbiddenException;
import com.yovexa.solutions.exception.ResourceNotFoundException;
import com.yovexa.solutions.exception.UnauthorizedException;
import com.yovexa.solutions.mapper.EntityMapper;
import com.yovexa.solutions.model.Admin;
import com.yovexa.solutions.repository.AdminRepository;
import com.yovexa.solutions.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final EntityMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final com.yovexa.solutions.service.RefreshTokenService refreshTokenService;

    @Override
    public AdminProfileResponse getProfile(String email) {
        Admin admin = adminRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with email: " + email));
        return mapper.toAdminProfileResponse(admin);
    }

    @Override
    @CacheEvict(value = "admins", allEntries = true)
    public void changePassword(String email, ChangePasswordRequest request) {
        Admin admin = adminRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UnauthorizedException("Admin account not found."));

        if (!passwordEncoder.matches(request.getCurrentPassword(), admin.getPassword())) {
            throw new UnauthorizedException("Current password does not match.");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match.");
        }

        if (passwordEncoder.matches(request.getNewPassword(), admin.getPassword())) {
            throw new IllegalArgumentException("New password cannot be the same as current password.");
        }

        admin.setPassword(passwordEncoder.encode(request.getNewPassword()));
        admin.setUpdatedAt(Instant.now());
        adminRepository.save(admin);

        // Security: Revoke all existing refresh token sessions on password change
        refreshTokenService.revokeAllForAdmin(admin.getId(), "PASSWORD_CHANGED");
    }

    @Override
    @Cacheable(value = "admins", key = "'all'")
    public List<AdminResponse> getAllAdmins() {
        return adminRepository.findAll()
                .stream()
                .map(mapper::toAdminResponse)
                .toList();
    }

    @Override
    @Cacheable(value = "admins", key = "#id")
    public AdminResponse getAdminById(String id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));
        return mapper.toAdminResponse(admin);
    }

    @Override
    @CacheEvict(value = "admins", allEntries = true)
    public AdminResponse createAdmin(CreateAdminRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (adminRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("An account with this email already exists.");
        }

        Admin admin = Admin.builder()
                .name(request.getName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role("ADMIN")
                .isActive(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Admin saved = adminRepository.save(admin);
        return mapper.toAdminResponse(saved);
    }

    @Override
    @CacheEvict(value = "admins", allEntries = true)
    public AdminResponse updateAdmin(String id, UpdateAdminRequest request) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));

        if (request.getName() != null && !request.getName().isBlank()) {
            admin.setName(request.getName().trim());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().trim().toLowerCase();
            if (!newEmail.equalsIgnoreCase(admin.getEmail()) && adminRepository.existsByEmailIgnoreCase(newEmail)) {
                throw new DuplicateResourceException("An account with this email already exists.");
            }
            admin.setEmail(newEmail);
        }

        if (request.getIsActive() != null) {
            // Guard: Cannot deactivate the last admin if only one admin exists
            if (Boolean.FALSE.equals(request.getIsActive()) && adminRepository.count() <= 1) {
                throw new ForbiddenException("Cannot deactivate the only remaining administrator.");
            }
            admin.setIsActive(request.getIsActive());
            if (Boolean.FALSE.equals(request.getIsActive())) {
                refreshTokenService.revokeAllForAdmin(admin.getId(), "ADMIN_DEACTIVATED");
            }
        }

        // Strict: Keep role = ADMIN (prevent arbitrary privilege elevation or alteration)
        admin.setRole("ADMIN");
        admin.setUpdatedAt(Instant.now());

        Admin updated = adminRepository.save(admin);
        return mapper.toAdminResponse(updated);
    }

    @Override
    @CacheEvict(value = "admins", allEntries = true)
    public void deleteAdmin(String id, String authenticatedAdminEmail) {
        if (adminRepository.count() <= 1) {
            throw new ForbiddenException("The last administrator cannot be deleted.");
        }

        Admin adminToDelete = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));

        if (authenticatedAdminEmail != null && adminToDelete.getEmail().equalsIgnoreCase(authenticatedAdminEmail.trim())) {
            throw new ForbiddenException("Administrators cannot delete their own account. Please have another administrator perform this action.");
        }

        refreshTokenService.revokeAllForAdmin(adminToDelete.getId(), "ADMIN_DELETED");
        adminRepository.delete(adminToDelete);
    }
}
