package com.yovexa.solutions.dto.project;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequest {

    @NotBlank(message = "Project name is required")
    @JsonAlias({"title", "projectName"})
    @Schema(description = "Project name", example = "Example Project")
    private String name;

    @Schema(description = "URL slug for project (auto-generated if omitted)", example = "example-project")
    private String slug;

    @Schema(description = "One-line architecture or subtitle", example = "Enterprise Sales Rep & Route Management")
    private String subtitle;

    @JsonAlias({"statusBadge", "badge"})
    @Schema(description = "Status badge tag", example = "Production / Enterprise System")
    private String clientLabel;

    @NotBlank(message = "Short description is required")
    @JsonAlias("summary")
    @Schema(description = "Short teaser description", example = "Example project description")
    private String shortDescription;

    @JsonAlias({"solution", "fullDescription"})
    @Schema(description = "Detailed project description", example = "Detailed project description")
    private String description;

    @Builder.Default
    @Schema(description = "Project category", example = "Web Application")
    private String category = "WEB_APPLICATIONS"; // WEB_APPLICATIONS, MOBILE_APPS, E_COMMERCE

    @Schema(description = "Project type", example = "Full Stack Web App")
    private String projectType;

    @JsonAlias({"image", "thumbnailUrl"})
    @Schema(description = "Featured cover image URL", example = "https://images.unsplash.com/photo-1555066931-4365d14bab8c")
    private String featuredImage;

    @Schema(description = "Key features / highlights")
    private List<String> features;

    @Schema(description = "Technologies used in project", example = "[\"React\", \"Spring Boot\", \"MongoDB\"]")
    private List<String> technologies;

    @JsonAlias("liveUrl")
    @Schema(description = "Live demo URL", example = "https://example.com")
    private String projectUrl;

    @Schema(description = "GitHub source code URL", example = "https://github.com/example/project")
    private String githubUrl;

    @Schema(description = "Case study documentation URL", example = "https://example.com/case-study")
    private String caseStudyUrl;

    @Builder.Default
    @Schema(description = "Publication status (DRAFT, PUBLISHED)", example = "PUBLISHED")
    private String status = "DRAFT"; // DRAFT, PUBLISHED

    @Builder.Default
    @Schema(description = "Whether the project is featured on the homepage", example = "false")
    private Boolean featured = false;

    @Builder.Default
    @Schema(description = "Display sort order", example = "0")
    private int displayOrder = 0;

    @Schema(description = "SEO meta title", example = "Example Project | Yovexa Solutions")
    private String seoTitle;

    @Schema(description = "SEO meta description", example = "Comprehensive overview of Example Project.")
    private String seoDescription;
}
