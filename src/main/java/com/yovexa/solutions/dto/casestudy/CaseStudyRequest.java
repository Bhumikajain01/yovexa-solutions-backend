package com.yovexa.solutions.dto.casestudy;

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
public class CaseStudyRequest {

    @NotBlank(message = "Case study title is required")
    @Schema(description = "Case study title", example = "Scaling Enterprise Order Sync & Telemetry in Real-Time")
    private String title;

    @Schema(description = "URL slug for case study (auto-generated if omitted)", example = "scaling-order-sync-telemetry")
    private String slug;

    @Schema(description = "Subtitle or architecture tagline", example = "High-concurrency distributed offline sync engine")
    private String subtitle;

    @Schema(description = "Optional related project reference or product name", example = "FieldTrack Pro / Yovexa CRM")
    private String projectReference;

    @Builder.Default
    @Schema(description = "Domain category", example = "WEB_APPLICATIONS")
    private String category = "WEB_APPLICATIONS";

    @Schema(description = "Architecture or client badge label", example = "Enterprise Architecture")
    private String clientLabel;

    @NotBlank(message = "Executive summary is required")
    @Schema(description = "Executive briefing / overview of the case study", example = "Executive briefing on the business and engineering scope...")
    private String summary;

    @NotBlank(message = "Problem statement is required")
    @Schema(description = "Business challenge and problem statement", example = "The client struggled with offline order synchronization latency...")
    private String problem;

    @NotBlank(message = "Technical solution is required")
    @Schema(description = "Technical solution & implementation details", example = "Yovexa engineered a multi-layered event-driven sync pipeline...")
    private String solution;

    @Schema(description = "Key architectural features")
    private List<String> features;

    @Schema(description = "Technologies used in case study", example = "[\"Kafka\", \"Mongodb\", \"Spring Boot\", \"React\"]")
    private List<String> technologies;

    @JsonAlias("image")
    @Schema(description = "Architecture diagram or cover image URL", example = "https://images.unsplash.com/photo-...")
    private String featuredImage;

    @JsonAlias("projectUrl")
    @Schema(description = "Live demo or reference URL", example = "https://example.com")
    private String liveUrl;

    @Schema(description = "Architecture / GitHub repository URL", example = "https://github.com/example/arch")
    private String githubUrl;

    @Builder.Default
    @Schema(description = "Publication status (DRAFT, PUBLISHED)", example = "PUBLISHED")
    private String status = "PUBLISHED";

    @Builder.Default
    @Schema(description = "Whether marked as featured", example = "false")
    private Boolean featured = false;

    @Builder.Default
    @Schema(description = "Display sort order", example = "0")
    private int displayOrder = 0;

    @Schema(description = "SEO meta title", example = "Scaling Enterprise Order Sync | Case Study")
    private String seoTitle;

    @Schema(description = "SEO meta description", example = "Deep dive into real-time order sync architecture.")
    private String seoDescription;
}
