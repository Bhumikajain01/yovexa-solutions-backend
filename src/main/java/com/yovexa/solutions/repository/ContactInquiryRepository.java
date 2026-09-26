package com.yovexa.solutions.repository;

import com.yovexa.solutions.model.ContactInquiry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactInquiryRepository extends MongoRepository<ContactInquiry, String> {
    long countByStatus(String status);



    List<ContactInquiry> findTop5ByOrderByCreatedAtDesc();
}
