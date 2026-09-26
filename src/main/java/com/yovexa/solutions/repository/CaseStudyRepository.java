package com.yovexa.solutions.repository;

import com.yovexa.solutions.model.CaseStudy;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaseStudyRepository extends MongoRepository<CaseStudy, String> {

    Optional<CaseStudy> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, String id);

    @Query(value = "{ $or: [ { 'status': ?0 }, { 'status': { $exists: false } }, { 'status': null }, { 'status': '' } ] }", sort = "{ 'displayOrder': 1, 'createdAt': -1 }")
    List<CaseStudy> findByStatusOrderByDisplayOrderAsc(String status);

    @Query(value = "{ $and: [ { $or: [ { 'status': ?0 }, { 'status': { $exists: false } }, { 'status': null }, { 'status': '' } ] }, { 'category': { $regex: ?1, $options: 'i' } } ] }", sort = "{ 'displayOrder': 1, 'createdAt': -1 }")
    List<CaseStudy> findByStatusAndCategoryOrderByDisplayOrderAsc(String status, String category);

    @Query(value = "{ $or: [ { 'status': ?0 }, { 'status': { $exists: false } }, { 'status': null }, { 'status': '' } ] }", count = true)
    long countByStatus(String status);



    List<CaseStudy> findTop5ByOrderByCreatedAtDesc();
}
