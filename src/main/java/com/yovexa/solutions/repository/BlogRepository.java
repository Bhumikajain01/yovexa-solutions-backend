package com.yovexa.solutions.repository;

import com.yovexa.solutions.model.Blog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogRepository extends MongoRepository<Blog, String> {
    Optional<Blog> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, String id);

    List<Blog> findByStatusOrderByPublishedAtDesc(String status);

    List<Blog> findByStatusAndCategoryOrderByPublishedAtDesc(String status, String category);

    long countByStatus(String status);



    List<Blog> findTop5ByOrderByCreatedAtDesc();
}
