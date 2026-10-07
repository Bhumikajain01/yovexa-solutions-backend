package com.yovexa.solutions.service.impl;

import com.yovexa.solutions.dto.hero.HeroRequest;
import com.yovexa.solutions.dto.hero.HeroResponse;
import com.yovexa.solutions.exception.ResourceNotFoundException;
import com.yovexa.solutions.mapper.EntityMapper;
import com.yovexa.solutions.model.HeroSection;
import com.yovexa.solutions.repository.HeroSectionRepository;
import com.yovexa.solutions.service.HeroService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HeroServiceImpl implements HeroService {

    private final HeroSectionRepository heroRepository;
    private final EntityMapper mapper;

    @Override
    @Cacheable(value = "heroes", key = "'all'")
    public List<HeroResponse> getAllHeroes() {
        return heroRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(mapper::toHeroResponse)
                .toList();
    }

    @Override
    @Cacheable(value = "heroes", key = "#id")
    public HeroResponse getHeroById(String id) {
        HeroSection hero = heroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HeroSection", "id", id));
        return mapper.toHeroResponse(hero);
    }

    @Override
    @Cacheable(value = "heroes", key = "'active'", unless = "#result == null")
    public HeroResponse getActiveHero() {
        return heroRepository.findFirstByIsActiveTrue()
                .or(() -> heroRepository.findFirstByStatusOrderByCreatedAtDesc("PUBLISHED"))
                .map(mapper::toHeroResponse)
                .orElse(null);
    }

    @Override
    @CacheEvict(value = "heroes", allEntries = true)
    public HeroResponse createHero(HeroRequest request) {
        HeroSection hero = mapper.toHeroSection(request);

        if (hero.isActive()) {
            deactivateAllHeroes();
        }

        HeroSection saved = heroRepository.save(hero);
        return mapper.toHeroResponse(saved);
    }

    @Override
    @CacheEvict(value = "heroes", allEntries = true)
    public HeroResponse updateHero(String id, HeroRequest request) {
        HeroSection existing = heroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HeroSection", "id", id));

        existing.setEyebrow(request.getEyebrow());
        existing.setHeadline(request.getHeadline());
        existing.setHighlightedHeadline(request.getHighlightedHeadline());
        existing.setDescription(request.getDescription());
        existing.setHeroImage(request.getHeroImage());
        existing.setHeroImageAlt(request.getHeroImageAlt());
        existing.setPrimaryCtaLabel(request.getPrimaryCtaLabel());
        existing.setPrimaryCtaLink(request.getPrimaryCtaLink());
        existing.setSecondaryCtaLabel(request.getSecondaryCtaLabel());
        existing.setSecondaryCtaLink(request.getSecondaryCtaLink());

        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus().toUpperCase());
        }

        if (request.getIsActive() != null) {
            if (request.getIsActive() && !existing.isActive()) {
                deactivateAllHeroes();
            }
            existing.setActive(request.getIsActive());
        }

        HeroSection saved = heroRepository.save(existing);
        return mapper.toHeroResponse(saved);
    }

    @Override
    @CacheEvict(value = "heroes", allEntries = true)
    public void deleteHero(String id) {
        if (!heroRepository.existsById(id)) {
            throw new ResourceNotFoundException("HeroSection", "id", id);
        }
        heroRepository.deleteById(id);
    }

    private void deactivateAllHeroes() {
        List<HeroSection> heroes = heroRepository.findAll();
        heroes.forEach(h -> h.setActive(false));
        heroRepository.saveAll(heroes);
    }
}
