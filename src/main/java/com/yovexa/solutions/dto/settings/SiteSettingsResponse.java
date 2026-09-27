package com.yovexa.solutions.dto.settings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteSettingsResponse {
    private String id;
    private String contactEmail;
    private String phone;
    private String whatsapp;
    private String location;
    private String address;
    private String workingHours;

    private String footerDescription;
    private String copyrightText;

    private String linkedin;
    private String github;
    private String instagram;
    private String facebook;

    private java.util.List<String> budgetOptions;

    private Instant updatedAt;
}
