package com.yovexa.solutions.service.impl;

import com.yovexa.solutions.dto.about.AboutRequest;
import com.yovexa.solutions.dto.about.AboutResponse;
import com.yovexa.solutions.exception.ResourceNotFoundException;
import com.yovexa.solutions.mapper.EntityMapper;
import com.yovexa.solutions.model.AboutSection;
import com.yovexa.solutions.repository.AboutSectionRepository;
import com.yovexa.solutions.service.AboutService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AboutServiceImpl implements AboutService {

    private final AboutSectionRepository aboutRepository;
    private final EntityMapper mapper;

    @Override
    @Cacheable(value = "about", key = "'all'")
    public List<AboutResponse> getAllAboutSections() {
        return aboutRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(mapper::toAboutResponse)
                .toList();
    }

    @Override
    @Cacheable(value = "about", key = "#id")
    public AboutResponse getAboutById(String id) {
        AboutSection about = aboutRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AboutSection", "id", id));
        return mapper.toAboutResponse(about);
    }

    @Override
    @Cacheable(value = "about", key = "'active'", unless = "#result == null")
    public AboutResponse getActiveAbout() {
        return aboutRepository.findFirstByIsActiveTrue()
                .or(() -> aboutRepository.findFirstByStatusOrderByCreatedAtDesc("PUBLISHED"))
                .map(mapper::toAboutResponse)
                .orElse(null);
    }

    @Override
    @CacheEvict(value = "about", allEntries = true)
    public AboutResponse createAbout(AboutRequest request) {
        AboutSection about = mapper.toAboutSection(request);

        if (about.isActive()) {
            deactivateAllAboutSections();
        }

        AboutSection saved = aboutRepository.save(about);
        return mapper.toAboutResponse(saved);
    }

    @Override
    @CacheEvict(value = "about", allEntries = true)
    public AboutResponse updateAbout(String id, AboutRequest request) {
        AboutSection existing = aboutRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AboutSection", "id", id));

        existing.setEyebrow(request.getEyebrow());
        existing.setTitle(request.getTitle());
        existing.setHighlightedTitle(request.getHighlightedTitle());
        existing.setPrimaryParagraph(request.getPrimaryParagraph());
        existing.setSecondaryParagraph(request.getSecondaryParagraph());
        existing.setPrimaryButtonLabel(request.getPrimaryButtonLabel());
        existing.setPrimaryButtonLink(request.getPrimaryButtonLink());
        existing.setSecondaryButtonLabel(request.getSecondaryButtonLabel());
        existing.setSecondaryButtonLink(request.getSecondaryButtonLink());
        existing.setImage(request.getImage());
        existing.setImageAlt(request.getImageAlt());
        existing.setImageCategory(request.getImageCategory());
        existing.setImageTitle(request.getImageTitle());
        existing.setImageBadge(request.getImageBadge());

        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus().toUpperCase());
        }

        if (request.getIsActive() != null) {
            if (request.getIsActive() && !existing.isActive()) {
                deactivateAllAboutSections();
            }
            existing.setActive(request.getIsActive());
        }

        AboutSection saved = aboutRepository.save(existing);
        return mapper.toAboutResponse(saved);
    }

    @Override
    @CacheEvict(value = "about", allEntries = true)
    public void deleteAbout(String id) {
        if (!aboutRepository.existsById(id)) {
            throw new ResourceNotFoundException("AboutSection", "id", id);
        }
        aboutRepository.deleteById(id);
    }

    private void deactivateAllAboutSections() {
        List<AboutSection> sections = aboutRepository.findAll();
        sections.forEach(s -> s.setActive(false));
        aboutRepository.saveAll(sections);
    }
}
