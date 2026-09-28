package com.yovexa.solutions.controller;

import com.yovexa.solutions.dto.about.AboutRequest;
import com.yovexa.solutions.dto.about.AboutResponse;
import com.yovexa.solutions.dto.common.ApiResponse;
import com.yovexa.solutions.service.AboutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@Tag(name = "About", description = "About Section Content Management APIs")
public class AboutController {

    private final AboutService aboutService;

    // Public Endpoint
    @GetMapping({"/api/about/active", "/api/content/about/active"})
    @Operation(summary = "Get active about section (Public)", description = "Fetches the single currently active published about section for the website.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Active about section retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No active about section found")
    })
    public ResponseEntity<ApiResponse<AboutResponse>> getActiveAbout() {
        AboutResponse about = aboutService.getActiveAbout();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic().staleWhileRevalidate(10, TimeUnit.MINUTES))
                .body(ApiResponse.success(about));
    }

    // Admin Endpoints
    @GetMapping({"/api/admin/about", "/api/admin/content/about"})
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List all about records (Admin)", description = "Retrieves all about section records for administration.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "List of about sections retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<ApiResponse<List<AboutResponse>>> getAllAboutSections() {
        List<AboutResponse> abouts = aboutService.getAllAboutSections();
        return ResponseEntity.ok(ApiResponse.success(abouts));
    }

    @GetMapping({"/api/admin/about/{id}", "/api/admin/content/about/{id}"})
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get about section by ID (Admin)", description = "Fetches a specific about section record by its MongoDB ID.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "About section retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "About section not found")
    })
    public ResponseEntity<ApiResponse<AboutResponse>> getAboutById(
            @io.swagger.v3.oas.annotations.Parameter(description = "About section unique MongoDB ID", required = true)
            @PathVariable String id) {
        AboutResponse about = aboutService.getAboutById(id);
        return ResponseEntity.ok(ApiResponse.success(about));
    }

    @PostMapping({"/api/admin/about", "/api/admin/content/about"})
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create about section (Admin)", description = "Creates a new about section entry.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "About section created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed - title or primaryParagraph missing"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required")
    })
    public ResponseEntity<ApiResponse<AboutResponse>> createAbout(
            @Valid @org.springframework.web.bind.annotation.RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "About section details", required = true)
            AboutRequest request) {
        AboutResponse about = aboutService.createAbout(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("About section created successfully", about));
    }

    @PutMapping({"/api/admin/about/{id}", "/api/admin/content/about/{id}"})
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update about section (Admin)", description = "Updates an existing about section by ID.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "About section updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "About section not found")
    })
    public ResponseEntity<ApiResponse<AboutResponse>> updateAbout(
            @io.swagger.v3.oas.annotations.Parameter(description = "About section unique MongoDB ID", required = true)
            @PathVariable String id,
            @Valid @org.springframework.web.bind.annotation.RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated about section details", required = true)
            AboutRequest request) {
        AboutResponse about = aboutService.updateAbout(id, request);
        return ResponseEntity.ok(ApiResponse.success("About section updated successfully", about));
    }

    @DeleteMapping({"/api/admin/about/{id}", "/api/admin/content/about/{id}"})
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete about section (Admin)", description = "Deletes an about section record by ID.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "About section deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "About section not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteAbout(
            @io.swagger.v3.oas.annotations.Parameter(description = "About section unique MongoDB ID", required = true)
            @PathVariable String id) {
        aboutService.deleteAbout(id);
        return ResponseEntity.ok(ApiResponse.successMessage("About section deleted successfully"));
    }
}
