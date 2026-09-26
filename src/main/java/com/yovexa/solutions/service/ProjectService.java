package com.yovexa.solutions.service;

import com.yovexa.solutions.dto.common.PagedResponse;
import com.yovexa.solutions.dto.project.ProjectRequest;
import com.yovexa.solutions.dto.project.ProjectResponse;

import java.util.List;

public interface ProjectService {
    List<ProjectResponse> getPublicProjects(String category, String search);
    ProjectResponse getProjectBySlug(String slug);
    PagedResponse<ProjectResponse> getAdminProjects(String search, String category, String status, int page, int size);
    ProjectResponse getProjectById(String id);
    ProjectResponse createProject(ProjectRequest request);
    ProjectResponse updateProject(String id, ProjectRequest request);
    void deleteProject(String id);
    java.util.List<java.util.Map<String, String>> getProjectCategories();
}
