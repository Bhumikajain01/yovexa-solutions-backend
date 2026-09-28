package com.yovexa.solutions.controller;

import com.yovexa.solutions.dto.casestudy.CaseStudyRequest;
import com.yovexa.solutions.dto.casestudy.CaseStudyResponse;
import com.yovexa.solutions.dto.common.ApiResponse;
import com.yovexa.solutions.dto.common.PagedResponse;
import com.yovexa.solutions.service.CaseStudyService;
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
@Tag(name = "Case Studies", description = "Architectural and Technical Case Studies Management APIs")
public class CaseStudyController {

    private final CaseStudyService caseStudyService;

    // Public Endpoints
    @GetMapping("/api/case-studies")
    @Operation(summary = "Get published case studies (Public)", description = "Retrieves published case studies with optional category and search filters.")
    public ResponseEntity<ApiResponse<List<CaseStudyResponse>>> getPublicCaseStudies(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {
        List<CaseStudyResponse> caseStudies = caseStudyService.getPublicCaseStudies(category, search);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic().staleWhileRevalidate(10, TimeUnit.MINUTES))
                .body(ApiResponse.success(caseStudies));
    }

    @GetMapping("/api/case-studies/categories")
    @Operation(summary = "Get case study categories (Public)", description = "Retrieves case study categories dynamically from backend.")
    public ResponseEntity<ApiResponse<java.util.List<java.util.Map<String, String>>>> getCaseStudyCategories() {
        java.util.List<java.util.Map<String, String>> categories = caseStudyService.getCaseStudyCategories();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(15, TimeUnit.MINUTES).cachePublic().staleWhileRevalidate(30, TimeUnit.MINUTES))
                .body(ApiResponse.success(categories));
    }

    @GetMapping("/api/case-studies/{slug}")
    @Operation(summary = "Get published case study by slug (Public)", description = "Retrieves a published case study by its unique URL slug.")
    public ResponseEntity<ApiResponse<CaseStudyResponse>> getCaseStudyBySlug(
            @PathVariable String slug) {
        CaseStudyResponse caseStudy = caseStudyService.getCaseStudyBySlug(slug);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic().staleWhileRevalidate(10, TimeUnit.MINUTES))
                .body(ApiResponse.success(caseStudy));
    }

    // Admin Endpoints
    @GetMapping("/api/admin/case-studies")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List all case studies with pagination and filters (Admin)", description = "Retrieves case studies with pagination, category, status and search filters for administration.")
    public ResponseEntity<ApiResponse<PagedResponse<CaseStudyResponse>>> getAdminCaseStudies(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<CaseStudyResponse> response = caseStudyService.getAdminCaseStudies(search, category, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/api/admin/case-studies/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get case study by ID (Admin)", description = "Fetches a case study record by its MongoDB ID.")
    public ResponseEntity<ApiResponse<CaseStudyResponse>> getCaseStudyById(
            @PathVariable String id) {
        CaseStudyResponse caseStudy = caseStudyService.getCaseStudyById(id);
        return ResponseEntity.ok(ApiResponse.success(caseStudy));
    }

    @PostMapping("/api/admin/case-studies")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create case study (Admin)", description = "Creates a new case study record.")
    public ResponseEntity<ApiResponse<CaseStudyResponse>> createCaseStudy(
            @Valid @RequestBody CaseStudyRequest request) {
        CaseStudyResponse caseStudy = caseStudyService.createCaseStudy(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Case study created successfully", caseStudy));
    }

    @PutMapping("/api/admin/case-studies/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update case study (Admin)", description = "Updates an existing case study record by ID.")
    public ResponseEntity<ApiResponse<CaseStudyResponse>> updateCaseStudy(
            @PathVariable String id,
            @Valid @RequestBody CaseStudyRequest request) {
        CaseStudyResponse caseStudy = caseStudyService.updateCaseStudy(id, request);
        return ResponseEntity.ok(ApiResponse.success("Case study updated successfully", caseStudy));
    }

    @DeleteMapping("/api/admin/case-studies/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete case study (Admin)", description = "Deletes a case study record by ID.")
    public ResponseEntity<ApiResponse<Void>> deleteCaseStudy(
            @PathVariable String id) {
        caseStudyService.deleteCaseStudy(id);
        return ResponseEntity.ok(ApiResponse.successMessage("Case study deleted successfully"));
    }
}
