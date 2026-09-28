package com.yovexa.solutions.controller;

import com.yovexa.solutions.dto.common.ApiResponse;
import com.yovexa.solutions.dto.hero.HeroRequest;
import com.yovexa.solutions.dto.hero.HeroResponse;
import com.yovexa.solutions.service.HeroService;
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
@Tag(name = "Hero", description = "Hero Section Content Management APIs")
public class HeroController {

    private final HeroService heroService;

    // Public Endpoint
    @GetMapping({"/api/hero/active", "/api/content/hero/active"})
    @Operation(summary = "Get active hero section (Public)", description = "Fetches the single currently active published hero section for the website landing page.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Active hero section retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No active hero section found")
    })
    public ResponseEntity<ApiResponse<HeroResponse>> getActiveHero() {
        HeroResponse hero = heroService.getActiveHero();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic().staleWhileRevalidate(10, TimeUnit.MINUTES))
                .body(ApiResponse.success(hero));
    }

    // Admin Endpoints
    @GetMapping({"/api/admin/hero", "/api/admin/content/hero"})
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List all hero records (Admin)", description = "Retrieves all hero sections (both drafts and published) for administration.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "List of hero sections retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<ApiResponse<List<HeroResponse>>> getAllHeroes() {
        List<HeroResponse> heroes = heroService.getAllHeroes();
        return ResponseEntity.ok(ApiResponse.success(heroes));
    }

    @GetMapping({"/api/admin/hero/{id}", "/api/admin/content/hero/{id}"})
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get hero section by ID (Admin)", description = "Fetches a specific hero section record by its MongoDB ID.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Hero section retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Hero section not found")
    })
    public ResponseEntity<ApiResponse<HeroResponse>> getHeroById(
            @io.swagger.v3.oas.annotations.Parameter(description = "Hero section unique MongoDB ID", required = true)
            @PathVariable String id) {
        HeroResponse hero = heroService.getHeroById(id);
        return ResponseEntity.ok(ApiResponse.success(hero));
    }

    @PostMapping({"/api/admin/hero", "/api/admin/content/hero"})
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create hero section (Admin)", description = "Creates a new hero section entry.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Hero section created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed - invalid headline or description"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required")
    })
    public ResponseEntity<ApiResponse<HeroResponse>> createHero(
            @Valid @org.springframework.web.bind.annotation.RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Hero section details", required = true)
            HeroRequest request) {
        HeroResponse hero = heroService.createHero(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Hero section created successfully", hero));
    }

    @PutMapping({"/api/admin/hero/{id}", "/api/admin/content/hero/{id}"})
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update hero section (Admin)", description = "Updates an existing hero section by ID.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Hero section updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Hero section not found")
    })
    public ResponseEntity<ApiResponse<HeroResponse>> updateHero(
            @io.swagger.v3.oas.annotations.Parameter(description = "Hero section unique MongoDB ID", required = true)
            @PathVariable String id,
            @Valid @org.springframework.web.bind.annotation.RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated hero section details", required = true)
            HeroRequest request) {
        HeroResponse hero = heroService.updateHero(id, request);
        return ResponseEntity.ok(ApiResponse.success("Hero section updated successfully", hero));
    }

    @DeleteMapping({"/api/admin/hero/{id}", "/api/admin/content/hero/{id}"})
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete hero section (Admin)", description = "Deletes a hero section record by ID.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Hero section deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Hero section not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteHero(
            @io.swagger.v3.oas.annotations.Parameter(description = "Hero section unique MongoDB ID", required = true)
            @PathVariable String id) {
        heroService.deleteHero(id);
        return ResponseEntity.ok(ApiResponse.successMessage("Hero section deleted successfully"));
    }
}
