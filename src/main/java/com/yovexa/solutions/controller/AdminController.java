package com.yovexa.solutions.controller;

import com.yovexa.solutions.dto.admin.*;
import com.yovexa.solutions.dto.common.ApiResponse;
import com.yovexa.solutions.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin", description = "Admin CRUD, Profile & Password Management APIs")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/profile")
    @Operation(summary = "Get admin profile", description = "Returns currently logged-in administrator's profile details.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile fetched successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - invalid or expired JWT"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Administrator profile not found")
    })
    public ResponseEntity<ApiResponse<AdminProfileResponse>> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        AdminProfileResponse profile = adminService.getProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Profile fetched successfully.", profile));
    }

    @PutMapping("/profile/password")
    @Operation(summary = "Change password", description = "Updates password for currently logged-in administrator.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password changed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed - current password incorrect or passwords do not match"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - invalid or expired JWT")
    })
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @org.springframework.web.bind.annotation.RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Password change payload", required = true)
            ChangePasswordRequest request
    ) {
        adminService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.successMessage("Password changed successfully."));
    }

    @GetMapping("/admins")
    @Operation(summary = "List all administrators", description = "Fetches list of all administrator accounts.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Administrators fetched successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<ApiResponse<List<AdminResponse>>> getAllAdmins() {
        List<AdminResponse> admins = adminService.getAllAdmins();
        return ResponseEntity.ok(ApiResponse.success("Administrators fetched successfully.", admins));
    }

    @GetMapping("/admins/{id}")
    @Operation(summary = "Get administrator by ID", description = "Fetches a single administrator by unique MongoDB ID.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Administrator fetched successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Administrator not found")
    })
    public ResponseEntity<ApiResponse<AdminResponse>> getAdminById(
            @io.swagger.v3.oas.annotations.Parameter(description = "Administrator unique ID", required = true)
            @PathVariable String id) {
        AdminResponse admin = adminService.getAdminById(id);
        return ResponseEntity.ok(ApiResponse.success("Administrator fetched successfully.", admin));
    }

    @PostMapping("/admins")
    @Operation(summary = "Create an administrator", description = "Creates a new administrator account (Requires ADMIN authentication).")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Administrator created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email already in use")
    })
    public ResponseEntity<ApiResponse<AdminResponse>> createAdmin(
            @Valid @org.springframework.web.bind.annotation.RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "New admin account details", required = true)
            CreateAdminRequest request) {
        AdminResponse response = adminService.createAdmin(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Administrator created successfully.", response));
    }

    @PutMapping("/admins/{id}")
    @Operation(summary = "Update administrator", description = "Updates administrator name, email, or active status.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Administrator updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Administrator not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email already exists on another account")
    })
    public ResponseEntity<ApiResponse<AdminResponse>> updateAdmin(
            @io.swagger.v3.oas.annotations.Parameter(description = "Administrator unique ID", required = true)
            @PathVariable String id,
            @Valid @org.springframework.web.bind.annotation.RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated admin details", required = true)
            UpdateAdminRequest request
    ) {
        AdminResponse response = adminService.updateAdmin(id, request);
        return ResponseEntity.ok(ApiResponse.success("Administrator updated successfully.", response));
    }

    @DeleteMapping("/admins/{id}")
    @Operation(summary = "Delete administrator", description = "Deletes an administrator account. Prevents deleting the last remaining administrator.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Administrator deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request - cannot delete the last remaining administrator"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Administrator not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteAdmin(
            @io.swagger.v3.oas.annotations.Parameter(description = "Administrator unique ID", required = true)
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        adminService.deleteAdmin(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.successMessage("Administrator deleted successfully."));
    }
}
