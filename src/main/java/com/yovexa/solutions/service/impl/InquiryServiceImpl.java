package com.yovexa.solutions.service.impl;

import com.yovexa.solutions.dto.common.PagedResponse;
import com.yovexa.solutions.dto.inquiry.ContactInquiryRequest;
import com.yovexa.solutions.dto.inquiry.ContactInquiryResponse;
import com.yovexa.solutions.dto.inquiry.InquiryStatusUpdateRequest;
import com.yovexa.solutions.exception.ResourceNotFoundException;
import com.yovexa.solutions.mapper.EntityMapper;
import com.yovexa.solutions.model.ContactInquiry;
import com.yovexa.solutions.repository.ContactInquiryRepository;
import com.yovexa.solutions.service.InquiryService;
import lombok.RequiredArgsConstructor;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class InquiryServiceImpl implements InquiryService {

    private final ContactInquiryRepository inquiryRepository;
    private final EntityMapper mapper;
    private final com.yovexa.solutions.service.EmailService emailService;
    private final MongoTemplate mongoTemplate;

    @Override
    public ContactInquiryResponse submitInquiry(ContactInquiryRequest request) {
        ContactInquiry inquiry = mapper.toContactInquiry(request);
        ContactInquiry saved = inquiryRepository.save(inquiry);
        emailService.sendInquiryNotification(saved);
        return mapper.toInquiryResponse(saved);
    }

    @Override
    public PagedResponse<ContactInquiryResponse> getAdminInquiries(String search, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            String s = search.trim();
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("fullName").regex(s, "i"),
                    Criteria.where("email").regex(s, "i"),
                    Criteria.where("companyName").regex(s, "i"),
                    Criteria.where("message").regex(s, "i")
            ));
        }

        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            criteriaList.add(Criteria.where("status").is(status.trim()));
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        long total = mongoTemplate.count(query, ContactInquiry.class);
        query.with(pageable);
        List<ContactInquiry> list = mongoTemplate.find(query, ContactInquiry.class);
        Page<ContactInquiry> pageResult = new PageImpl<>(list, pageable, total);

        return PagedResponse.of(pageResult.map(mapper::toInquiryResponse));
    }

    @Override
    public ContactInquiryResponse getInquiryById(String id) {
        ContactInquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ContactInquiry", "id", id));
        return mapper.toInquiryResponse(inquiry);
    }

    @Override
    public ContactInquiryResponse updateInquiryStatus(String id, InquiryStatusUpdateRequest request) {
        ContactInquiry existing = inquiryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ContactInquiry", "id", id));

        existing.setStatus(request.getStatus().toUpperCase());
        ContactInquiry saved = inquiryRepository.save(existing);
        return mapper.toInquiryResponse(saved);
    }

    @Override
    public void deleteInquiry(String id) {
        if (!inquiryRepository.existsById(id)) {
            throw new ResourceNotFoundException("ContactInquiry", "id", id);
        }
        inquiryRepository.deleteById(id);
    }
}
