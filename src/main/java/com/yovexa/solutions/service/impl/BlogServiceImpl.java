package com.yovexa.solutions.service.impl;

import com.yovexa.solutions.dto.blog.BlogRequest;
import com.yovexa.solutions.dto.blog.BlogResponse;
import com.yovexa.solutions.dto.common.PagedResponse;
import com.yovexa.solutions.exception.DuplicateResourceException;
import com.yovexa.solutions.exception.ResourceNotFoundException;
import com.yovexa.solutions.mapper.EntityMapper;
import com.yovexa.solutions.model.Blog;
import com.yovexa.solutions.repository.BlogRepository;
import com.yovexa.solutions.service.BlogService;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;
    private final EntityMapper mapper;
    private final MongoTemplate mongoTemplate;

    @Override
    @Cacheable(value = "blogs", key = "'public-' + (#search != null ? #search : '') + '-' + (#category != null ? #category : 'all')")
    public List<BlogResponse> getPublicBlogs(String search, String category) {
        List<Blog> list;
        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("ALL")) {
            String norm = category.trim().toUpperCase().replaceAll("[\\s-]+", "_");
            String catRegex = norm.replace("_", "[ _-]*");
            Query q = new Query();
            q.addCriteria(Criteria.where("status").is("PUBLISHED"));
            q.addCriteria(Criteria.where("category").regex("(?i)^" + catRegex + "$"));
            q.with(Sort.by(Sort.Direction.DESC, "publishedAt"));
            list = mongoTemplate.find(q, Blog.class);
        } else {
            list = blogRepository.findByStatusOrderByPublishedAtDesc("PUBLISHED");
        }

        if (search != null && !search.trim().isEmpty()) {
            String q = search.trim().toLowerCase();
            list = list.stream()
                    .filter(b -> (b.getTitle() != null && b.getTitle().toLowerCase().contains(q)) ||
                            (b.getExcerpt() != null && b.getExcerpt().toLowerCase().contains(q)) ||
                            (b.getContent() != null && b.getContent().toLowerCase().contains(q)))
                    .toList();
        }

        return list.stream().map(mapper::toBlogResponse).toList();
    }

    @Override
    @Cacheable(value = "blogs", key = "'paged-' + (#search != null ? #search : '') + '-' + (#category != null ? #category : 'all') + '-' + #page + '-' + #size")
    public PagedResponse<BlogResponse> getPublicBlogsPaged(String search, String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());

        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("status").is("PUBLISHED"));

        if (search != null && !search.trim().isEmpty()) {
            String s = search.trim();
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("title").regex(s, "i"),
                    Criteria.where("excerpt").regex(s, "i"),
                    Criteria.where("content").regex(s, "i")
            ));
        }

        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("ALL")) {
            String norm = category.trim().toUpperCase().replaceAll("[\\s-]+", "_");
            String catRegex = norm.replace("_", "[ _-]*");
            criteriaList.add(Criteria.where("category").regex("(?i)^" + catRegex + "$"));
        }

        query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));

        long total = mongoTemplate.count(query, Blog.class);
        query.with(pageable);
        List<Blog> list = mongoTemplate.find(query, Blog.class);
        Page<Blog> pageResult = new PageImpl<>(list, pageable, total);

        return PagedResponse.of(pageResult.map(mapper::toBlogResponse));
    }

    @Override
    @Cacheable(value = "blogs", key = "'slug-' + #slug")
    public BlogResponse getBlogBySlug(String slug) {
        Blog blog = blogRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", "slug", slug));
        return mapper.toBlogResponse(blog);
    }

    @Override
    @Cacheable(value = "blogs", key = "'admin-' + (#search != null ? #search : '') + '-' + (#category != null ? #category : 'all') + '-' + (#status != null ? #status : 'all') + '-' + #page + '-' + #size")
    public PagedResponse<BlogResponse> getAdminBlogs(String search, String category, String status, int page,
            int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            String s = search.trim();
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("title").regex(s, "i"),
                    Criteria.where("excerpt").regex(s, "i"),
                    Criteria.where("content").regex(s, "i")
            ));
        }

        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("ALL")) {
            String norm = category.trim().toUpperCase().replaceAll("[\\s-]+", "_");
            String catRegex = norm.replace("_", "[ _-]*");
            criteriaList.add(Criteria.where("category").regex("(?i)^" + catRegex + "$"));
        }

        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            criteriaList.add(Criteria.where("status").is(status.trim()));
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        long total = mongoTemplate.count(query, Blog.class);
        query.with(pageable);
        List<Blog> list = mongoTemplate.find(query, Blog.class);
        Page<Blog> pageResult = new PageImpl<>(list, pageable, total);

        return PagedResponse.of(pageResult.map(mapper::toBlogResponse));
    }

    @Override
    @Cacheable(value = "blogs", key = "'id-' + #id")
    public BlogResponse getBlogById(String id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", "id", id));
        return mapper.toBlogResponse(blog);
    }

    @Override
    @CacheEvict(value = {"blogs", "dashboard"}, allEntries = true)
    public BlogResponse createBlog(BlogRequest request) {
        Blog blog = mapper.toBlog(request);

        if (blogRepository.existsBySlug(blog.getSlug())) {
            blog.setSlug(blog.getSlug() + "-" + System.currentTimeMillis());
        }

        Blog saved = blogRepository.save(blog);
        return mapper.toBlogResponse(saved);
    }

    @Override
    @CacheEvict(value = {"blogs", "dashboard"}, allEntries = true)
    public BlogResponse updateBlog(String id, BlogRequest request) {
        Blog existing = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", "id", id));

        existing.setTitle(request.getTitle());

        String targetSlug = (request.getSlug() != null && !request.getSlug().trim().isEmpty())
                ? SlugUtils.toSlug(request.getSlug())
                : existing.getSlug();

        if (!existing.getSlug().equals(targetSlug) && blogRepository.existsBySlugAndIdNot(targetSlug, id)) {
            throw new DuplicateResourceException("Blog", "slug", targetSlug);
        }
        existing.setSlug(targetSlug);

        existing.setExcerpt(request.getExcerpt());
        existing.setContent(request.getContent());
        existing.setFeaturedImage(request.getFeaturedImage());
        existing.setCategory(request.getCategory());
        existing.setAuthor(request.getAuthor());
        if (request.getTags() != null) {
            existing.setTags(request.getTags());
        }

        if (request.getStatus() != null) {
            String newStatus = request.getStatus().toUpperCase();
            if ("PUBLISHED".equals(newStatus) && existing.getPublishedAt() == null) {
                existing.setPublishedAt(Instant.now());
            }
            existing.setStatus(newStatus);
        }
        if (request.getPublishedAt() != null) {
            existing.setPublishedAt(request.getPublishedAt());
        }
        existing.setSeoTitle(request.getSeoTitle());
        existing.setSeoDescription(request.getSeoDescription());

        Blog saved = blogRepository.save(existing);
        return mapper.toBlogResponse(saved);
    }

    @Override
    @CacheEvict(value = {"blogs", "dashboard"}, allEntries = true)
    public void deleteBlog(String id) {
        if (!blogRepository.existsById(id)) {
            throw new ResourceNotFoundException("Blog", "id", id);
        }
        blogRepository.deleteById(id);
    }

    @Override
    @Cacheable(value = "blogs", key = "'categories'")
    public List<Map<String, String>> getBlogCategories() {
        Map<String, String> standard = new LinkedHashMap<>();
        standard.put("all", "All Articles");
        standard.put("WEB_APPLICATIONS", "Web Applications");
        standard.put("MOBILE_APPS", "Mobile Apps");
        standard.put("E_COMMERCE", "E-Commerce");

        try {
            List<String> distinctInDb = mongoTemplate.getCollection("blogs")
                    .distinct("category", String.class)
                    .into(new ArrayList<>());

            Set<String> excluded = Set.of("BUSINESS_SYSTEMS", "SAAS_PLATFORMS");
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
