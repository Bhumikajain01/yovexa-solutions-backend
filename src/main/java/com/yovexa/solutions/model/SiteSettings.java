package com.yovexa.solutions.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "site_settings")
public class SiteSettings {

    @Id
    @Builder.Default
    private String id = "default_settings";

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

    @LastModifiedDate
    private Instant updatedAt;
}
