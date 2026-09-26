package com.yovexa.solutions.service;

import com.yovexa.solutions.dto.casestudy.CaseStudyRequest;
import com.yovexa.solutions.dto.casestudy.CaseStudyResponse;
import com.yovexa.solutions.dto.common.PagedResponse;

import java.util.List;

public interface CaseStudyService {

    List<CaseStudyResponse> getPublicCaseStudies(String category, String search);

    CaseStudyResponse getCaseStudyBySlug(String slug);

    PagedResponse<CaseStudyResponse> getAdminCaseStudies(String search, String category, String status, int page, int size);

    CaseStudyResponse getCaseStudyById(String id);

    CaseStudyResponse createCaseStudy(CaseStudyRequest request);

    CaseStudyResponse updateCaseStudy(String id, CaseStudyRequest request);

    void deleteCaseStudy(String id);
    java.util.List<java.util.Map<String, String>> getCaseStudyCategories();
}
