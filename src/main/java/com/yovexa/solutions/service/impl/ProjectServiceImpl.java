package com.yovexa.solutions.service.impl;

import com.yovexa.solutions.dto.common.PagedResponse;
import com.yovexa.solutions.dto.project.ProjectRequest;
import com.yovexa.solutions.dto.project.ProjectResponse;
import com.yovexa.solutions.exception.DuplicateResourceException;
import com.yovexa.solutions.exception.ResourceNotFoundException;
import com.yovexa.solutions.mapper.EntityMapper;
import com.yovexa.solutions.model.Project;
import com.yovexa.solutions.repository.ProjectRepository;
import com.yovexa.solutions.service.ProjectService;
import com.yovexa.solutions.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final EntityMapper mapper;
    private final MongoTemplate mongoTemplate;

    @Override
    @Cacheable(value = "projects", key = "T(com.yovexa.solutions.util.CacheKeyUtils).publicListKey(#category, #search)")
    public List<ProjectResponse> getPublicProjects(String category, String search) {
        List<Project> list = projectRepository.findByStatusOrderByDisplayOrderAsc("PUBLISHED");

        String normCategory = com.yovexa.solutions.util.CacheKeyUtils.normalizeCategory(category);
        String normSearch = com.yovexa.solutions.util.CacheKeyUtils.normalizeSearch(search);

        if (!"all".equals(normCategory)) {
            String catRegex = normCategory.replace("_", "[ _-]*");
            list = list.stream()
                    .filter(p -> {
                        String pCat = p.getCategory() != null ? p.getCategory().toUpperCase().replaceAll("[\\s-]+", "_") : "WEB_APPLICATIONS";
                        return pCat.equals(normCategory) || pCat.matches("(?i).*" + catRegex + ".*");
                    })
                    .toList();
        }

        if (!normSearch.isEmpty()) {
            list = list.stream()
                    .filter(p -> (p.getName() != null && p.getName().toLowerCase().contains(normSearch)) ||
                            (p.getTitle() != null && p.getTitle().toLowerCase().contains(normSearch)) ||
                            (p.getSubtitle() != null && p.getSubtitle().toLowerCase().contains(normSearch)) ||
                            (p.getClientLabel() != null && p.getClientLabel().toLowerCase().contains(normSearch)) ||
                            (p.getShortDescription() != null && p.getShortDescription().toLowerCase().contains(normSearch)) ||
                            (p.getDescription() != null && p.getDescription().toLowerCase().contains(normSearch)) ||
                            (p.getTechnologies() != null && p.getTechnologies().stream().anyMatch(t -> t != null && t.toLowerCase().contains(normSearch))))
                    .toList();
        }

        return list.stream().map(mapper::toProjectResponse).toList();
    }

    @Override
    @Cacheable(value = "projects", key = "T(com.yovexa.solutions.util.CacheKeyUtils).slugKey(#slug)")
    public ProjectResponse getProjectBySlug(String slug) {
        Project project = projectRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "slug", slug));
        return mapper.toProjectResponse(project);
    }

    @Override
    @Cacheable(value = "projects", key = "T(com.yovexa.solutions.util.CacheKeyUtils).adminListKey(#search, #category, #status, #page, #size)")
    public PagedResponse<ProjectResponse> getAdminProjects(String search, String category, String status, int page,
            int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("displayOrder").ascending().and(Sort.by("createdAt").descending()));

        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            String s = search.trim();
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("name").regex(s, "i"),
                    Criteria.where("title").regex(s, "i"),
                    Criteria.where("subtitle").regex(s, "i"),
                    Criteria.where("clientLabel").regex(s, "i"),
                    Criteria.where("shortDescription").regex(s, "i"),
                    Criteria.where("description").regex(s, "i"),
                    Criteria.where("technologies").regex(s, "i")
            ));
        }

        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("ALL")) {
            criteriaList.add(Criteria.where("category").regex(category.trim(), "i"));
        }

        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            criteriaList.add(Criteria.where("status").is(status.trim()));
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        long total = mongoTemplate.count(query, Project.class);
        query.with(pageable);
        List<Project> list = mongoTemplate.find(query, Project.class);
        Page<Project> pageResult = new PageImpl<>(list, pageable, total);

        Page<ProjectResponse> dtoPage = pageResult.map(mapper::toProjectResponse);
        return PagedResponse.of(dtoPage);
    }

    @Override
    @Cacheable(value = "projects", key = "'id-' + #id")
    public ProjectResponse getProjectById(String id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        return mapper.toProjectResponse(project);
    }

    @Override
    @CacheEvict(value = {"projects", "dashboard"}, allEntries = true)
    public ProjectResponse createProject(ProjectRequest request) {
        Project project = mapper.toProject(request);

        if (projectRepository.existsBySlug(project.getSlug())) {
            throw new DuplicateResourceException("Project", "slug", project.getSlug());
        }

        Project saved = projectRepository.save(project);
        return mapper.toProjectResponse(saved);
    }

    @Override
    @CacheEvict(value = {"projects", "dashboard"}, allEntries = true)
    public ProjectResponse updateProject(String id, ProjectRequest request) {
        Project existing = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        existing.setName(request.getName());
        existing.setTitle(request.getName());

        String targetSlug = (request.getSlug() != null && !request.getSlug().trim().isEmpty())
                ? SlugUtils.toSlug(request.getSlug())
                : existing.getSlug();

        if (!existing.getSlug().equals(targetSlug) && projectRepository.existsBySlugAndIdNot(targetSlug, id)) {
            throw new DuplicateResourceException("Project", "slug", targetSlug);
        }
        existing.setSlug(targetSlug);

        existing.setSubtitle(request.getSubtitle());
        existing.setClientLabel(request.getClientLabel());
        existing.setShortDescription(request.getShortDescription());
        if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
            existing.setDescription(request.getDescription());
        } else if (request.getShortDescription() != null && !request.getShortDescription().trim().isEmpty()) {
            existing.setDescription(request.getShortDescription());
        }
        if (request.getCategory() != null) {
            existing.setCategory(request.getCategory().toUpperCase());
        }
        existing.setProjectType(request.getProjectType());
        existing.setFeaturedImage(request.getFeaturedImage());
        existing.setImage(request.getFeaturedImage());
        if (request.getFeatures() != null) {
            existing.setFeatures(request.getFeatures());
        }
        if (request.getTechnologies() != null) {
            existing.setTechnologies(request.getTechnologies());
        }
        existing.setProjectUrl(request.getProjectUrl());
        existing.setLiveUrl(request.getProjectUrl());
        existing.setGithubUrl(request.getGithubUrl());
        existing.setCaseStudyUrl(request.getCaseStudyUrl());

        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus().toUpperCase());
        }
        if (request.getFeatured() != null) {
            existing.setFeatured(request.getFeatured());
        }
        existing.setDisplayOrder(request.getDisplayOrder());
        existing.setSeoTitle(request.getSeoTitle());
        existing.setSeoDescription(request.getSeoDescription());

        Project saved = projectRepository.save(existing);
        return mapper.toProjectResponse(saved);
    }

    @Override
    @CacheEvict(value = {"projects", "dashboard"}, allEntries = true)
    public void deleteProject(String id) {
        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Project", "id", id);
        }
        projectRepository.deleteById(id);
    }

    @Override
    @Cacheable(value = "projects", key = "'categories'")
    public List<Map<String, String>> getProjectCategories() {
        Map<String, String> standard = new LinkedHashMap<>();
        standard.put("all", "All Projects");
        standard.put("WEB_APPLICATIONS", "Web Applications");
        standard.put("MOBILE_APPS", "Mobile Apps");
        standard.put("E_COMMERCE", "E-Commerce");

        try {
            List<String> distinctInDb = mongoTemplate.getCollection("projects")
                    .distinct("category", String.class)
                    .into(new ArrayList<>());

            java.util.Set<String> excluded = java.util.Set.of("BUSINESS_SYSTEMS", "SAAS_PLATFORMS");
            for (String cat : distinctInDb) {
                if (cat != null && !cat.trim().isEmpty()) {
                    String normKey = cat.trim().toUpperCase().replaceAll("[\\s-]+", "_");
                    if (!excluded.contains(normKey) && !standard.containsKey(normKey)) {
                        standard.put(normKey, formatCategoryLabel(cat));
                    }
                }
            }
        } catch (Exception ignored) {
        }

        List<Map<String, String>> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : standard.entrySet()) {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("id", entry.getKey());
            item.put("label", entry.getValue());
            result.add(item);
        }
        return result;
    }

    private String formatCategoryLabel(String raw) {
        if (raw == null || raw.isBlank()) return "General";
        String[] words = raw.replace('_', ' ').replace('-', ' ').split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                if (!sb.isEmpty()) sb.append(" ");
                sb.append(Character.toUpperCase(w.charAt(0)));
                if (w.length() > 1) {
                    sb.append(w.substring(1).toLowerCase());
                }
            }
        }
        return sb.toString();
    }
}
