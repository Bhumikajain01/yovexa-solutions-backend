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
@Document(collection = "projects")
public class Project {

    @Id
    private String id;

    private String name;
    private String title;

    @Indexed(unique = true)
    private String slug;

    private String subtitle;
    private String clientLabel;

    private String shortDescription;
    private String description;

    @Indexed
    private String category; // WEB_APPLICATIONS, MOBILE_APPS, E_COMMERCE

    private String projectType;

    private String featuredImage;
    private String image;

    @Builder.Default
    private List<String> features = new ArrayList<>();

    @Builder.Default
    private List<String> technologies = new ArrayList<>();

    private String projectUrl;
    private String liveUrl;
    private String githubUrl;
    private String caseStudyUrl;

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

    public String getName() {
        return (name != null && !name.trim().isEmpty()) ? name : title;
    }

    public String getTitle() {
        return (title != null && !title.trim().isEmpty()) ? title : name;
    }

    public String getFeaturedImage() {
        return (featuredImage != null && !featuredImage.trim().isEmpty()) ? featuredImage : image;
    }

    public String getImage() {
        return (image != null && !image.trim().isEmpty()) ? image : featuredImage;
    }

    public String getProjectUrl() {
        return (projectUrl != null && !projectUrl.trim().isEmpty()) ? projectUrl : liveUrl;
    }

    public String getLiveUrl() {
        return (liveUrl != null && !liveUrl.trim().isEmpty()) ? liveUrl : projectUrl;
    }

    public String getShortDescription() {
        if (shortDescription != null && !shortDescription.trim().isEmpty()) {
            return shortDescription;
        }
        if (description != null && description.length() > 160) {
            return description.substring(0, 157) + "...";
        }
        return description;
    }

    public String getStatus() {
        return (status != null && !status.trim().isEmpty()) ? status : "PUBLISHED";
    }

    public List<String> getFeatures() {
        return features != null ? features : new ArrayList<>();
    }

    public List<String> getTechnologies() {
        return technologies != null ? technologies : new ArrayList<>();
    }

    public String getCategory() {
        return (category != null && !category.trim().isEmpty()) ? category : "WEB_APPLICATIONS";
    }
}
