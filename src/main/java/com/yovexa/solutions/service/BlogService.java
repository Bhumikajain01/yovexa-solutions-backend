package com.yovexa.solutions.service;

import com.yovexa.solutions.dto.blog.BlogRequest;
import com.yovexa.solutions.dto.blog.BlogResponse;
import com.yovexa.solutions.dto.common.PagedResponse;

import java.util.List;

public interface BlogService {
    List<BlogResponse> getPublicBlogs(String search, String category);
    PagedResponse<BlogResponse> getPublicBlogsPaged(String search, String category, int page, int size);
    BlogResponse getBlogBySlug(String slug);
    PagedResponse<BlogResponse> getAdminBlogs(String search, String category, String status, int page, int size);
    BlogResponse getBlogById(String id);
    BlogResponse createBlog(BlogRequest request);
    BlogResponse updateBlog(String id, BlogRequest request);
    void deleteBlog(String id);
    java.util.List<java.util.Map<String, String>> getBlogCategories();
}
