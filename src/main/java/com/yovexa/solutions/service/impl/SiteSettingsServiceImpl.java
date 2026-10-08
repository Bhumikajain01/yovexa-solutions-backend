package com.yovexa.solutions.service.impl;

import com.yovexa.solutions.dto.settings.SiteSettingsRequest;
import com.yovexa.solutions.dto.settings.SiteSettingsResponse;
import com.yovexa.solutions.mapper.EntityMapper;
import com.yovexa.solutions.model.SiteSettings;
import com.yovexa.solutions.repository.SiteSettingsRepository;
import com.yovexa.solutions.service.SiteSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SiteSettingsServiceImpl implements SiteSettingsService {

    private final SiteSettingsRepository settingsRepository;
    private final EntityMapper mapper;

    @Override
    @Cacheable(value = "settings", key = "'site_settings'")
    public SiteSettingsResponse getSettings() {
        return settingsRepository.getSettings()
                .map(mapper::toSiteSettingsResponse)
                .orElseGet(() -> mapper.toSiteSettingsResponse(
                        SiteSettings.builder()
                                .id("default_settings")
                                .build()
                ));
    }

    @Override
    @CacheEvict(value = "settings", allEntries = true)
    public SiteSettingsResponse updateSettings(SiteSettingsRequest request) {
        SiteSettings settings = settingsRepository.getSettings()
                .orElseGet(() -> SiteSettings.builder().id("default_settings").build());

        mapper.updateSiteSettings(settings, request);
        settings.setUpdatedAt(Instant.now());

        SiteSettings saved = settingsRepository.save(settings);
        return mapper.toSiteSettingsResponse(saved);
    }
}
