package com.yovexa.solutions.dto.casestudy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseStudyResponse {
    private String id;
    private String title;
    private String slug;
    private String subtitle;
    private String projectReference;
    private String category;
    private String clientLabel;
    private String summary;
    private String problem;
    private String solution;
    private List<String> features;
    private List<String> technologies;
    private String featuredImage;
    private String liveUrl;
    private String githubUrl;
    private String status;
    private boolean featured;
    private int displayOrder;
    private String seoTitle;
    private String seoDescription;
    private Instant createdAt;
    private Instant updatedAt;
}
