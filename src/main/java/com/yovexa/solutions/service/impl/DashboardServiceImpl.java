package com.yovexa.solutions.service.impl;

import com.yovexa.solutions.dto.dashboard.DashboardResponse;
import com.yovexa.solutions.mapper.EntityMapper;
import com.yovexa.solutions.repository.*;
import com.yovexa.solutions.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ProjectRepository projectRepository;
    private final ServiceRepository serviceRepository;
    private final BlogRepository blogRepository;
    private final ContactInquiryRepository inquiryRepository;
    private final EntityMapper mapper;

    @Override
    @Cacheable(value = "dashboard", key = "'stats'")
    public DashboardResponse getDashboardStats() {
        long totalProjects = projectRepository.count();
        long publishedProjects = projectRepository.countByStatus("PUBLISHED");
        long totalServices = serviceRepository.count();
        long publishedBlogs = blogRepository.countByStatus("PUBLISHED");
        long draftBlogs = blogRepository.countByStatus("DRAFT");
        long totalInquiries = inquiryRepository.count();
        long newInquiries = inquiryRepository.countByStatus("NEW");

        var recentBlogs = blogRepository.findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(mapper::toBlogResponse)
                .toList();

        var recentProjects = projectRepository.findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(mapper::toProjectResponse)
                .toList();

        var recentInquiries = inquiryRepository.findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(mapper::toInquiryResponse)
                .toList();

        return DashboardResponse.builder()
                .projects(totalProjects)
                .publishedProjects(publishedProjects)
                .services(totalServices)
                .publishedBlogs(publishedBlogs)
                .draftBlogs(draftBlogs)
                .inquiries(totalInquiries)
                .newInquiries(newInquiries)
                .recentBlogs(recentBlogs)
                .recentProjects(recentProjects)
                .recentInquiries(recentInquiries)
                .build();
    }
}
