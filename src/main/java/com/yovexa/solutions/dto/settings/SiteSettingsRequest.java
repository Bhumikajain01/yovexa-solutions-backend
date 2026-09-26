package com.yovexa.solutions.dto.settings;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteSettingsRequest {
    @Schema(description = "Primary contact email address", example = "info@yovexasolutions.com")
    private String contactEmail;

    @Schema(description = "Customer service telephone number", example = "+1 (555) 123-4567")
    private String phone;

    @Schema(description = "WhatsApp contact number or link", example = "+1 (555) 987-6543")
    private String whatsapp;

    @Schema(description = "General location / city, country", example = "San Francisco, CA, USA")
    private String location;

    @Schema(description = "Detailed physical office address", example = "100 Innovation Way, Suite 400")
    private String address;

    @Schema(description = "Business working hours", example = "Mon - Fri: 9:00 AM - 6:00 PM")
    private String workingHours;

    @Schema(description = "Footer descriptive text", example = "Empowering global companies with modern technology and digital innovation.")
    private String footerDescription;

    @Schema(description = "Footer copyright text", example = "© 2026 Yovexa Solutions. All rights reserved.")
    private String copyrightText;

    @Schema(description = "LinkedIn page URL", example = "https://linkedin.com/company/yovexa-solutions")
    private String linkedin;

    @Schema(description = "GitHub organization URL", example = "https://github.com/yovexa-solutions")
    private String github;

    @Schema(description = "Instagram profile URL", example = "https://instagram.com/yovexasolutions")
    private String instagram;

    @Schema(description = "Facebook page URL", example = "https://facebook.com/yovexasolutions")
    private String facebook;
}
