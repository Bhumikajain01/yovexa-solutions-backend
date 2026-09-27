package com.yovexa.solutions.service.impl;

import com.yovexa.solutions.dto.settings.SiteSettingsRequest;
import com.yovexa.solutions.dto.settings.SiteSettingsResponse;
import com.yovexa.solutions.mapper.EntityMapper;
import com.yovexa.solutions.model.SiteSettings;
import com.yovexa.solutions.repository.SiteSettingsRepository;
import com.yovexa.solutions.service.SiteSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SiteSettingsServiceImpl implements SiteSettingsService {

    private final SiteSettingsRepository settingsRepository;
    private final EntityMapper mapper;

    public static final java.util.List<String> DEFAULT_BUDGET_OPTIONS = java.util.List.of(
            "Under ₹25,000",
            "₹25,000 – ₹50,000",
            "₹50,000 – ₹1,00,000",
            "₹1,00,000+",
            "Not Sure Yet"
    );

    @Override
    public SiteSettingsResponse getSettings() {
        return settingsRepository.getSettings()
                .map(settings -> {
                    if (settings.getBudgetOptions() == null || settings.getBudgetOptions().isEmpty()) {
                        settings.setBudgetOptions(new java.util.ArrayList<>(DEFAULT_BUDGET_OPTIONS));
                    }
                    return mapper.toSiteSettingsResponse(settings);
                })
                .orElseGet(() -> mapper.toSiteSettingsResponse(
                        SiteSettings.builder()
                                .id("default_settings")
                                .budgetOptions(new java.util.ArrayList<>(DEFAULT_BUDGET_OPTIONS))
                                .build()
                ));
    }

    @Override
    public SiteSettingsResponse updateSettings(SiteSettingsRequest request) {
        SiteSettings settings = settingsRepository.getSettings()
                .orElseGet(() -> SiteSettings.builder().id("default_settings").build());

        mapper.updateSiteSettings(settings, request);
        settings.setUpdatedAt(Instant.now());

        SiteSettings saved = settingsRepository.save(settings);
        return mapper.toSiteSettingsResponse(saved);
    }
}
