package com.yovexa.solutions.controller;

import com.yovexa.solutions.dto.common.ApiResponse;
import com.yovexa.solutions.dto.common.PagedResponse;
import com.yovexa.solutions.dto.project.ProjectRequest;
import com.yovexa.solutions.dto.project.ProjectResponse;
import com.yovexa.solutions.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Portfolio and Case Studies Management APIs")
public class ProjectController {

    private final ProjectService projectService;

    // Public Endpoints
    @GetMapping("/api/projects")
    @Operation(summary = "Get published projects (Public)", description = "Retrieves published projects with optional category and search filters for the portfolio section.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Published projects retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getPublicProjects(
            @io.swagger.v3.oas.annotations.Parameter(description = "Filter by category (e.g. WEB_APPLICATIONS, MOBILE_APPS, E_COMMERCE)", required = false)
            @RequestParam(required = false) String category,
            @io.swagger.v3.oas.annotations.Parameter(description = "Search keyword in project name or summary", required = false)
            @RequestParam(required = false) String search) {
        List<ProjectResponse> projects = projectService.getPublicProjects(category, search);
        return ResponseEntity.ok(ApiResponse.success(projects));
    }

    @GetMapping("/api/projects/categories")
    @Operation(summary = "Get project categories (Public)", description = "Retrieves project categories dynamically from backend.")
    public ResponseEntity<ApiResponse<java.util.List<java.util.Map<String, String>>>> getProjectCategories() {
        java.util.List<java.util.Map<String, String>> categories = projectService.getProjectCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/api/projects/{slug}")
    @Operation(summary = "Get published project by slug (Public)", description = "Retrieves a published project by its unique URL slug.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Project details retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found with the specified slug")
    })
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectBySlug(
            @io.swagger.v3.oas.annotations.Parameter(description = "Project unique URL slug", required = true)
            @PathVariable String slug) {
        ProjectResponse project = projectService.getProjectBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(project));
    }

    // Admin Endpoints
    @GetMapping("/api/admin/projects")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List all projects with pagination and filters (Admin)", description = "Retrieves projects with pagination, category, status and search filters for administration.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paged projects retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<ApiResponse<PagedResponse<ProjectResponse>>> getAdminProjects(
            @io.swagger.v3.oas.annotations.Parameter(description = "Search keyword", required = false)
            @RequestParam(required = false) String search,
            @io.swagger.v3.oas.annotations.Parameter(description = "Category filter", required = false)
            @RequestParam(required = false) String category,
            @io.swagger.v3.oas.annotations.Parameter(description = "Publication status filter (DRAFT, PUBLISHED)", required = false)
            @RequestParam(required = false) String status,
            @io.swagger.v3.oas.annotations.Parameter(description = "Zero-indexed page number", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @io.swagger.v3.oas.annotations.Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<ProjectResponse> response = projectService.getAdminProjects(search, category, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/admin/projects/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get project by ID (Admin)", description = "Fetches a project record by its MongoDB ID.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Project retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(
            @io.swagger.v3.oas.annotations.Parameter(description = "Project unique MongoDB ID", required = true)
            @PathVariable String id) {
        ProjectResponse project = projectService.getProjectById(id);
        return ResponseEntity.ok(ApiResponse.success(project));
    }

    @PostMapping("/api/admin/projects")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create project (Admin)", description = "Creates a new project record.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Project created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed - name or shortDescription missing"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict - project with identical slug already exists")
    })
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @org.springframework.web.bind.annotation.RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "New project creation details", required = true)
            ProjectRequest request) {
        ProjectResponse project = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Project created successfully", project));
    }

    @PutMapping("/api/admin/projects/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update project (Admin)", description = "Updates an existing project record by ID.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Project updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict - updated slug collides with another project")
    })
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @io.swagger.v3.oas.annotations.Parameter(description = "Project unique MongoDB ID", required = true)
            @PathVariable String id,
            @Valid @org.springframework.web.bind.annotation.RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated project details", required = true)
            ProjectRequest request) {
        ProjectResponse project = projectService.updateProject(id, request);
        return ResponseEntity.ok(ApiResponse.success("Project updated successfully", project));
    }

    @DeleteMapping("/api/admin/projects/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete project (Admin)", description = "Deletes a project record by ID.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Project deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @io.swagger.v3.oas.annotations.Parameter(description = "Project unique MongoDB ID", required = true)
            @PathVariable String id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.successMessage("Project deleted successfully"));
    }
}
