package com.yovexa.solutions.service.impl;

import com.yovexa.solutions.dto.casestudy.CaseStudyRequest;
import com.yovexa.solutions.dto.casestudy.CaseStudyResponse;
import com.yovexa.solutions.dto.common.PagedResponse;
import com.yovexa.solutions.exception.DuplicateResourceException;
import com.yovexa.solutions.exception.ResourceNotFoundException;
import com.yovexa.solutions.mapper.EntityMapper;
import com.yovexa.solutions.model.CaseStudy;
import com.yovexa.solutions.repository.CaseStudyRepository;
import com.yovexa.solutions.service.CaseStudyService;
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
public class CaseStudyServiceImpl implements CaseStudyService {

    private final CaseStudyRepository caseStudyRepository;
    private final EntityMapper mapper;
    private final MongoTemplate mongoTemplate;

    @Override
    @Cacheable(value = "caseStudies", key = "T(com.yovexa.solutions.util.CacheKeyUtils).publicListKey(#category, #search)")
    public List<CaseStudyResponse> getPublicCaseStudies(String category, String search) {
        List<CaseStudy> list = caseStudyRepository.findByStatusOrderByDisplayOrderAsc("PUBLISHED");

        String normCategory = com.yovexa.solutions.util.CacheKeyUtils.normalizeCategory(category);
        String normSearch = com.yovexa.solutions.util.CacheKeyUtils.normalizeSearch(search);

        if (!"all".equals(normCategory)) {
            String catRegex = normCategory.replace("_", "[ _-]*");
            list = list.stream()
                    .filter(cs -> {
                        String csCat = cs.getCategory() != null ? cs.getCategory().toUpperCase().replaceAll("[\\s-]+", "_") : "WEB_APPLICATIONS";
                        return csCat.equals(normCategory) || csCat.matches("(?i).*" + catRegex + ".*");
                    })
                    .toList();
        }

        if (!normSearch.isEmpty()) {
            list = list.stream()
                    .filter(cs -> (cs.getTitle() != null && cs.getTitle().toLowerCase().contains(normSearch)) ||
                            (cs.getSubtitle() != null && cs.getSubtitle().toLowerCase().contains(normSearch)) ||
                            (cs.getSummary() != null && cs.getSummary().toLowerCase().contains(normSearch)) ||
                            (cs.getProblem() != null && cs.getProblem().toLowerCase().contains(normSearch)) ||
                            (cs.getSolution() != null && cs.getSolution().toLowerCase().contains(normSearch)))
                    .toList();
        }

        return list.stream().map(mapper::toCaseStudyResponse).toList();
    }

    @Override
    @Cacheable(value = "caseStudies", key = "T(com.yovexa.solutions.util.CacheKeyUtils).slugKey(#slug)")
    public CaseStudyResponse getCaseStudyBySlug(String slug) {
        CaseStudy caseStudy = caseStudyRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("CaseStudy", "slug", slug));
        return mapper.toCaseStudyResponse(caseStudy);
    }

    @Override
    @Cacheable(value = "caseStudies", key = "T(com.yovexa.solutions.util.CacheKeyUtils).adminListKey(#search, #category, #status, #page, #size)")
    public PagedResponse<CaseStudyResponse> getAdminCaseStudies(String search, String category, String status, int page,
            int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("displayOrder").ascending().and(Sort.by("createdAt").descending()));

        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            String safeSearch = com.yovexa.solutions.util.MongoSecurityUtils.escapeRegex(search.trim());
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("title").regex(safeSearch, "i"),
                    Criteria.where("subtitle").regex(safeSearch, "i"),
                    Criteria.where("clientLabel").regex(safeSearch, "i"),
                    Criteria.where("summary").regex(safeSearch, "i"),
                    Criteria.where("problem").regex(safeSearch, "i"),
                    Criteria.where("solution").regex(safeSearch, "i"),
                    Criteria.where("technologies").regex(safeSearch, "i")
            ));
        }

        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("ALL")) {
            String safeCategory = com.yovexa.solutions.util.MongoSecurityUtils.escapeRegex(category.trim());
            criteriaList.add(Criteria.where("category").regex(safeCategory, "i"));
        }

        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            criteriaList.add(Criteria.where("status").is(status.trim()));
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        long total = mongoTemplate.count(query, CaseStudy.class);
        query.with(pageable);
        List<CaseStudy> list = mongoTemplate.find(query, CaseStudy.class);
        Page<CaseStudy> pageResult = new PageImpl<>(list, pageable, total);

        Page<CaseStudyResponse> dtoPage = pageResult.map(mapper::toCaseStudyResponse);
        return PagedResponse.of(dtoPage);
    }

    @Override
    @Cacheable(value = "caseStudies", key = "'id-' + #id")
    public CaseStudyResponse getCaseStudyById(String id) {
        CaseStudy caseStudy = caseStudyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CaseStudy", "id", id));
        return mapper.toCaseStudyResponse(caseStudy);
    }

    @Override
    @CacheEvict(value = "caseStudies", allEntries = true)
    public CaseStudyResponse createCaseStudy(CaseStudyRequest request) {
        CaseStudy caseStudy = mapper.toCaseStudy(request);

        if (caseStudyRepository.existsBySlug(caseStudy.getSlug())) {
            throw new DuplicateResourceException("CaseStudy", "slug", caseStudy.getSlug());
        }

        CaseStudy saved = caseStudyRepository.save(caseStudy);
        return mapper.toCaseStudyResponse(saved);
    }

    @Override
    @CacheEvict(value = "caseStudies", allEntries = true)
    public CaseStudyResponse updateCaseStudy(String id, CaseStudyRequest request) {
        CaseStudy existing = caseStudyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CaseStudy", "id", id));

        existing.setTitle(request.getTitle());

        String targetSlug = (request.getSlug() != null && !request.getSlug().trim().isEmpty())
                ? SlugUtils.toSlug(request.getSlug())
                : existing.getSlug();

        if (!existing.getSlug().equals(targetSlug) && caseStudyRepository.existsBySlugAndIdNot(targetSlug, id)) {
            throw new DuplicateResourceException("CaseStudy", "slug", targetSlug);
        }
        existing.setSlug(targetSlug);

        existing.setSubtitle(request.getSubtitle());
        existing.setProjectReference(request.getProjectReference());
        if (request.getCategory() != null) {
            existing.setCategory(request.getCategory().toUpperCase());
        }
        existing.setClientLabel(request.getClientLabel());
        existing.setSummary(request.getSummary());
        existing.setProblem(request.getProblem());
        existing.setSolution(request.getSolution());
        if (request.getFeatures() != null) {
            existing.setFeatures(request.getFeatures());
        }
        if (request.getTechnologies() != null) {
            existing.setTechnologies(request.getTechnologies());
        }
        existing.setFeaturedImage(request.getFeaturedImage());
        existing.setLiveUrl(request.getLiveUrl());
        existing.setGithubUrl(request.getGithubUrl());

        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus().toUpperCase());
        }
        if (request.getFeatured() != null) {
            existing.setFeatured(request.getFeatured());
        }
        existing.setDisplayOrder(request.getDisplayOrder());
        existing.setSeoTitle(request.getSeoTitle());
        existing.setSeoDescription(request.getSeoDescription());

        CaseStudy saved = caseStudyRepository.save(existing);
        return mapper.toCaseStudyResponse(saved);
    }

    @Override
    @CacheEvict(value = "caseStudies", allEntries = true)
    public void deleteCaseStudy(String id) {
        if (!caseStudyRepository.existsById(id)) {
            throw new ResourceNotFoundException("CaseStudy", "id", id);
        }
        caseStudyRepository.deleteById(id);
    }

    @Override
    @Cacheable(value = "caseStudies", key = "'categories'")
    public List<Map<String, String>> getCaseStudyCategories() {
        Map<String, String> standard = new LinkedHashMap<>();
        standard.put("all", "All Case Studies");
        standard.put("WEB_APPLICATIONS", "Web Applications");
        standard.put("MOBILE_APPS", "Mobile Apps");
        standard.put("E_COMMERCE", "E-Commerce");

        try {
            List<String> distinctInDb = mongoTemplate.getCollection("case_studies")
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
