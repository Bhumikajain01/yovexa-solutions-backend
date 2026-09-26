package com.yovexa.solutions.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "case_studies")
public class CaseStudy {

    @Id
    private String id;

    private String title;

    @Indexed(unique = true)
    private String slug;

    private String subtitle;
    private String projectReference;

    @Indexed
    private String category; // WEB_APPLICATIONS, MOBILE_APPS, E_COMMERCE

    private String clientLabel;

    private String summary;
    private String problem;
    private String solution;

    @Builder.Default
    private List<String> features = new ArrayList<>();

    @Builder.Default
    private List<String> technologies = new ArrayList<>();

    private String featuredImage;
    private String liveUrl;
    private String githubUrl;

    @Builder.Default
    @Indexed
    private String status = "PUBLISHED"; // DRAFT, PUBLISHED

    @Builder.Default
    @Indexed
    private boolean featured = false;

    @Builder.Default
    @Indexed
    private int displayOrder = 0;

    private String seoTitle;
    private String seoDescription;

    @CreatedDate
    @Indexed
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public List<String> getFeatures() {
        return features != null ? features : new ArrayList<>();
    }

    public List<String> getTechnologies() {
        return technologies != null ? technologies : new ArrayList<>();
    }
}
