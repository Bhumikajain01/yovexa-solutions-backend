package com.yovexa.solutions.controller;

import com.yovexa.solutions.dto.common.ApiResponse;
import com.yovexa.solutions.dto.settings.SiteSettingsRequest;
import com.yovexa.solutions.dto.settings.SiteSettingsResponse;
import com.yovexa.solutions.service.SiteSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Site Settings", description = "Global Site Contact, Footer & Social Settings APIs")
public class SiteSettingsController {

    private final SiteSettingsService siteSettingsService;

    // Public Endpoints
    @GetMapping({"/api/site-settings", "/api/content/settings", "/api/content/footer", "/api/content/contact"})
    @Operation(summary = "Get site settings (Public)", description = "Retrieves global singleton site contact, address, footer and social media settings for the website.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Site settings retrieved successfully")
    })
    public ResponseEntity<ApiResponse<SiteSettingsResponse>> getSettings() {
        SiteSettingsResponse settings = siteSettingsService.getSettings();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache().mustRevalidate())
                .body(ApiResponse.success(settings));
    }

    @GetMapping({"/api/site-settings/budget-options", "/api/content/budget-options"})
    @Operation(summary = "Get estimated budget options (Public)", description = "Retrieves the dynamic list of project budget options for inquiry forms.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Budget options retrieved successfully")
    })
    public ResponseEntity<ApiResponse<java.util.List<String>>> getBudgetOptions() {
        SiteSettingsResponse settings = siteSettingsService.getSettings();
        java.util.List<String> options = settings != null && settings.getBudgetOptions() != null
                ? settings.getBudgetOptions()
                : java.util.Collections.emptyList();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache().mustRevalidate())
                .body(ApiResponse.success(options));
    }

    // Admin Endpoints
    @GetMapping({"/api/admin/site-settings", "/api/admin/content/settings", "/api/admin/content/footer", "/api/admin/content/contact"})
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get site settings (Admin)", description = "Retrieves current site contact, footer and social configuration for admin management.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Site settings retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<ApiResponse<SiteSettingsResponse>> getAdminSettings() {
        SiteSettingsResponse settings = siteSettingsService.getSettings();
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @PutMapping({"/api/admin/site-settings", "/api/admin/content/settings", "/api/admin/content/footer", "/api/admin/content/contact"})
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update site settings (Admin)", description = "Updates site contact email, phone, location, footer description, copyright, and social links.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Site settings updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<ApiResponse<SiteSettingsResponse>> updateSettings(
            @org.springframework.web.bind.annotation.RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated site settings details", required = true)
            SiteSettingsRequest request) {
        SiteSettingsResponse settings = siteSettingsService.updateSettings(request);
        return ResponseEntity.ok(ApiResponse.success("Site settings updated successfully", settings));
    }
}
