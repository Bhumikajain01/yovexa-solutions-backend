package com.yovexa.solutions.controller;

import com.yovexa.solutions.dto.common.ApiResponse;
import com.yovexa.solutions.dto.service.ServiceRequest;
import com.yovexa.solutions.dto.service.ServiceResponse;
import com.yovexa.solutions.service.ServicesService;
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
@Tag(name = "Services", description = "Services Management APIs")
public class ServiceController {

    private final ServicesService servicesService;

    // Public Endpoints
    @GetMapping("/api/services")
    @Operation(summary = "Get active services (Public)", description = "Retrieves all active services sorted by displayOrder ASC for the website services showcase.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "List of active services retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<ServiceResponse>>> getActiveServices() {
        List<ServiceResponse> services = servicesService.getActiveServices();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic().staleWhileRevalidate(10, TimeUnit.MINUTES))
                .body(ApiResponse.success(services));
    }

    @GetMapping("/api/services/{slug}")
    @Operation(summary = "Get service by slug (Public)", description = "Retrieves a single published service by its URL slug.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Service details retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Service not found with the specified slug")
    })
    public ResponseEntity<ApiResponse<ServiceResponse>> getServiceBySlug(
            @io.swagger.v3.oas.annotations.Parameter(description = "Service unique URL slug (e.g. web-development)", required = true)
            @PathVariable String slug) {
        ServiceResponse service = servicesService.getServiceBySlug(slug);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic().staleWhileRevalidate(10, TimeUnit.MINUTES))
                .body(ApiResponse.success(service));
    }

    // Admin Endpoints
    @GetMapping("/api/admin/services")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List all services (Admin)", description = "Retrieves all services (active and inactive) for administration.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "All services retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<ApiResponse<List<ServiceResponse>>> getAllServices() {
        List<ServiceResponse> services = servicesService.getAllServices();
        return ResponseEntity.ok(ApiResponse.success(services));
    }

    @GetMapping("/api/admin/services/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get service by ID (Admin)", description = "Fetches a service by its unique MongoDB ID.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Service retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Service not found")
    })
    public ResponseEntity<ApiResponse<ServiceResponse>> getServiceById(
            @io.swagger.v3.oas.annotations.Parameter(description = "Service unique MongoDB ID", required = true)
            @PathVariable String id) {
        ServiceResponse service = servicesService.getServiceById(id);
        return ResponseEntity.ok(ApiResponse.success(service));
    }

    @PostMapping("/api/admin/services")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create new service (Admin)", description = "Creates a new service entry.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Service created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict - service with identical slug already exists")
    })
    public ResponseEntity<ApiResponse<ServiceResponse>> createService(
            @Valid @org.springframework.web.bind.annotation.RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "New service creation details", required = true)
            ServiceRequest request) {
        ServiceResponse service = servicesService.createService(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Service created successfully", service));
    }

    @PutMapping("/api/admin/services/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update service (Admin)", description = "Updates an existing service by ID.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Service updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Service not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict - updated slug collides with existing service")
    })
    public ResponseEntity<ApiResponse<ServiceResponse>> updateService(
            @io.swagger.v3.oas.annotations.Parameter(description = "Service unique MongoDB ID", required = true)
            @PathVariable String id,
            @Valid @org.springframework.web.bind.annotation.RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated service details", required = true)
            ServiceRequest request) {
        ServiceResponse service = servicesService.updateService(id, request);
        return ResponseEntity.ok(ApiResponse.success("Service updated successfully", service));
    }

    @DeleteMapping("/api/admin/services/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete service (Admin)", description = "Deletes a service by its ID.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Service deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Service not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteService(
            @io.swagger.v3.oas.annotations.Parameter(description = "Service unique MongoDB ID", required = true)
            @PathVariable String id) {
        servicesService.deleteService(id);
        return ResponseEntity.ok(ApiResponse.successMessage("Service deleted successfully"));
    }
}
