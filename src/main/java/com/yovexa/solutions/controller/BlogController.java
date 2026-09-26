package com.yovexa.solutions.controller;

import com.yovexa.solutions.dto.blog.BlogRequest;
import com.yovexa.solutions.dto.blog.BlogResponse;
import com.yovexa.solutions.dto.common.ApiResponse;
import com.yovexa.solutions.dto.common.PagedResponse;
import com.yovexa.solutions.service.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Blogs", description = "Blog Articles Management APIs")
public class BlogController {

    private final BlogService blogService;

    // Public Endpoints
    @GetMapping("/api/blogs")
    @Operation(summary = "Get published blogs (Public)", description = "Retrieves published blogs with optional search and category filters for the website blog section.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Published blogs retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<BlogResponse>>> getPublicBlogs(
            @io.swagger.v3.oas.annotations.Parameter(description = "Search keyword in blog title or excerpt", required = false)
            @RequestParam(required = false) String search,
            @io.swagger.v3.oas.annotations.Parameter(description = "Blog category filter", required = false)
            @RequestParam(required = false) String category) {
        List<BlogResponse> blogs = blogService.getPublicBlogs(search, category);
        return ResponseEntity.ok(ApiResponse.success(blogs));
    }

    @GetMapping("/api/blogs/categories")
    @Operation(summary = "Get blog categories (Public)", description = "Retrieves blog categories dynamically from backend.")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getBlogCategories() {
        List<Map<String, String>> categories = blogService.getBlogCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/api/blogs/{slug}")
    @Operation(summary = "Get published blog by slug (Public)", description = "Retrieves a published blog article by its unique URL slug.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Blog article retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blog article not found with the specified slug")
    })
    public ResponseEntity<ApiResponse<BlogResponse>> getBlogBySlug(
            @io.swagger.v3.oas.annotations.Parameter(description = "Blog unique URL slug", required = true)
            @PathVariable String slug) {
        BlogResponse blog = blogService.getBlogBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(blog));
    }

    // Admin Endpoints
    @GetMapping("/api/admin/blogs")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List all blogs with pagination and filters (Admin)", description = "Retrieves blogs with pagination, category, status and search filters for administration.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paged blogs retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<ApiResponse<PagedResponse<BlogResponse>>> getAdminBlogs(
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
        PagedResponse<BlogResponse> response = blogService.getAdminBlogs(search, category, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/admin/blogs/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get blog by ID (Admin)", description = "Fetches a blog record by its unique MongoDB ID.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Blog record retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blog not found")
    })
    public ResponseEntity<ApiResponse<BlogResponse>> getBlogById(
            @io.swagger.v3.oas.annotations.Parameter(description = "Blog unique MongoDB ID", required = true)
            @PathVariable String id) {
        BlogResponse blog = blogService.getBlogById(id);
        return ResponseEntity.ok(ApiResponse.success(blog));
    }

    @PostMapping("/api/admin/blogs")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create blog (Admin)", description = "Creates a new blog post.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Blog post created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed - missing title, excerpt, content, category, or author"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict - blog with identical slug already exists")
    })
    public ResponseEntity<ApiResponse<BlogResponse>> createBlog(
            @Valid @org.springframework.web.bind.annotation.RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "New blog post details", required = true)
            BlogRequest request) {
        BlogResponse blog = blogService.createBlog(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Blog created successfully", blog));
    }

    @PutMapping("/api/admin/blogs/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update blog (Admin)", description = "Updates an existing blog post by ID.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Blog post updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blog post not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict - updated slug collides with another blog post")
    })
    public ResponseEntity<ApiResponse<BlogResponse>> updateBlog(
            @io.swagger.v3.oas.annotations.Parameter(description = "Blog unique MongoDB ID", required = true)
            @PathVariable String id,
            @Valid @org.springframework.web.bind.annotation.RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated blog post details", required = true)
            BlogRequest request) {
        BlogResponse blog = blogService.updateBlog(id, request);
        return ResponseEntity.ok(ApiResponse.success("Blog updated successfully", blog));
    }

    @DeleteMapping("/api/admin/blogs/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete blog (Admin)", description = "Deletes a blog post record by ID.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Blog post deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - JWT required"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blog post not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteBlog(
            @io.swagger.v3.oas.annotations.Parameter(description = "Blog unique MongoDB ID", required = true)
            @PathVariable String id) {
        blogService.deleteBlog(id);
        return ResponseEntity.ok(ApiResponse.successMessage("Blog deleted successfully"));
    }
}
