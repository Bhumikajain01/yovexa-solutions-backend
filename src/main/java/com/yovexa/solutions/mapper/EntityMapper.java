package com.yovexa.solutions.mapper;

import com.yovexa.solutions.dto.about.AboutRequest;
import com.yovexa.solutions.dto.about.AboutResponse;
import com.yovexa.solutions.dto.auth.UserSummaryDto;
import com.yovexa.solutions.dto.blog.BlogRequest;
import com.yovexa.solutions.dto.blog.BlogResponse;
import com.yovexa.solutions.dto.hero.HeroRequest;
import com.yovexa.solutions.dto.hero.HeroResponse;
import com.yovexa.solutions.dto.inquiry.ContactInquiryRequest;
import com.yovexa.solutions.dto.inquiry.ContactInquiryResponse;
import com.yovexa.solutions.dto.casestudy.CaseStudyRequest;
import com.yovexa.solutions.dto.casestudy.CaseStudyResponse;
import com.yovexa.solutions.dto.project.ProjectRequest;
import com.yovexa.solutions.dto.project.ProjectResponse;
import com.yovexa.solutions.dto.service.ServiceRequest;
import com.yovexa.solutions.dto.service.ServiceResponse;
import com.yovexa.solutions.dto.settings.SiteSettingsRequest;
import com.yovexa.solutions.dto.settings.SiteSettingsResponse;
import com.yovexa.solutions.model.*;
import com.yovexa.solutions.util.SlugUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;
import com.yovexa.solutions.dto.admin.AdminProfileResponse;
import com.yovexa.solutions.dto.admin.AdminResponse;
import java.util.ArrayList;

@Component
public class EntityMapper {

    // ADMIN
    public UserSummaryDto toUserSummaryDto(Admin admin) {
        if (admin == null)
            return null;
        return UserSummaryDto.builder()
                .id(admin.getId())
                .name(admin.getName())
                .email(admin.getEmail())
                .role(admin.getRole())
                .build();
    }

    public AdminResponse toAdminResponse(Admin admin) {
        if (admin == null)
            return null;
        return AdminResponse.builder()
                .id(admin.getId())
                .name(admin.getName())
                .email(admin.getEmail())
                .role(admin.getRole())
                .isActive(admin.getIsActive() != null ? admin.getIsActive() : true)
                .createdAt(admin.getCreatedAt())
                .updatedAt(admin.getUpdatedAt())
                .build();
    }

    public AdminProfileResponse toAdminProfileResponse(Admin admin) {
        if (admin == null)
            return null;
        return AdminProfileResponse.builder()
                .id(admin.getId())
                .name(admin.getName())
                .email(admin.getEmail())
                .role(admin.getRole())
                .isActive(admin.getIsActive() != null ? admin.getIsActive() : true)
                .build();
    }

    // HERO
    public HeroResponse toHeroResponse(HeroSection hero) {
        if (hero == null)
            return null;
        return HeroResponse.builder()
                .id(hero.getId())
                .eyebrow(hero.getEyebrow())
                .badge(hero.getEyebrow())
                .headline(hero.getHeadline())
                .heading(hero.getHeadline())
                .highlightedHeadline(hero.getHighlightedHeadline())
                .highlightedText(hero.getHighlightedHeadline())
                .description(hero.getDescription())
                .heroImage(hero.getHeroImage())
                .heroImageAlt(hero.getHeroImageAlt())
                .primaryCtaLabel(hero.getPrimaryCtaLabel())
                .primaryCtaText(hero.getPrimaryCtaLabel())
                .primaryCtaLink(hero.getPrimaryCtaLink())
                .secondaryCtaLabel(hero.getSecondaryCtaLabel())
                .secondaryCtaText(hero.getSecondaryCtaLabel())
                .secondaryCtaLink(hero.getSecondaryCtaLink())
                .status(hero.getStatus())
                .isActive(hero.isActive())
                .createdAt(hero.getCreatedAt())
                .updatedAt(hero.getUpdatedAt())
                .build();
    }

    public HeroSection toHeroSection(HeroRequest request) {
        if (request == null)
            return null;
        return HeroSection.builder()
                .eyebrow(request.getEyebrow())
                .headline(request.getHeadline())
                .highlightedHeadline(request.getHighlightedHeadline())
                .description(request.getDescription())
                .heroImage(request.getHeroImage())
                .heroImageAlt(request.getHeroImageAlt())
                .primaryCtaLabel(request.getPrimaryCtaLabel())
                .primaryCtaLink(request.getPrimaryCtaLink())
                .secondaryCtaLabel(request.getSecondaryCtaLabel())
                .secondaryCtaLink(request.getSecondaryCtaLink())
                .status(request.getStatus() != null ? request.getStatus().toUpperCase() : "DRAFT")
                .isActive(request.getIsActive() != null && request.getIsActive())
                .build();
    }

    // ABOUT
    public AboutResponse toAboutResponse(AboutSection about) {
        if (about == null)
            return null;
        return AboutResponse.builder()
                .id(about.getId())
                .eyebrow(about.getEyebrow())
                .badge(about.getEyebrow())
                .sectionLabel(about.getEyebrow())
                .title(about.getTitle())
                .highlightedTitle(about.getHighlightedTitle())
                .titleHighlight(about.getHighlightedTitle())
                .primaryParagraph(about.getPrimaryParagraph())
                .description(about.getPrimaryParagraph())
                .secondaryParagraph(about.getSecondaryParagraph())
                .additionalDescription(about.getSecondaryParagraph())
                .primaryButtonLabel(about.getPrimaryButtonLabel())
                .primaryCtaText(about.getPrimaryButtonLabel())
                .primaryButtonLink(about.getPrimaryButtonLink())
                .primaryCtaLink(about.getPrimaryButtonLink())
                .secondaryButtonLabel(about.getSecondaryButtonLabel())
                .secondaryCtaText(about.getSecondaryButtonLabel())
                .secondaryButtonLink(about.getSecondaryButtonLink())
                .secondaryCtaLink(about.getSecondaryButtonLink())
                .image(about.getImage())
                .imageAlt(about.getImageAlt())
                .imageCategory(about.getImageCategory())
                .imageTitle(about.getImageTitle())
                .imageBadge(about.getImageBadge())
                .highlights(about.getHighlights() != null ? about.getHighlights() : new ArrayList<>())
                .status(about.getStatus())
                .isActive(about.isActive())
                .createdAt(about.getCreatedAt())
                .updatedAt(about.getUpdatedAt())
                .build();
    }

    public AboutSection toAboutSection(AboutRequest request) {
        if (request == null)
            return null;
        return AboutSection.builder()
                .eyebrow(request.getEyebrow())
                .title(request.getTitle())
                .highlightedTitle(request.getHighlightedTitle())
                .primaryParagraph(request.getPrimaryParagraph())
                .secondaryParagraph(request.getSecondaryParagraph())
                .primaryButtonLabel(request.getPrimaryButtonLabel())
                .primaryButtonLink(request.getPrimaryButtonLink())
                .secondaryButtonLabel(request.getSecondaryButtonLabel())
                .secondaryButtonLink(request.getSecondaryButtonLink())
                .image(request.getImage())
                .imageAlt(request.getImageAlt())
                .imageCategory(request.getImageCategory())
                .imageTitle(request.getImageTitle())
                .imageBadge(request.getImageBadge())
                .highlights(request.getHighlights() != null ? request.getHighlights() : new ArrayList<>())
                .status(request.getStatus() != null ? request.getStatus().toUpperCase() : "DRAFT")
                .isActive(request.getIsActive() != null && request.getIsActive())
                .build();
    }

    // SERVICE
    public ServiceResponse toServiceResponse(Service service) {
        if (service == null)
            return null;
        return ServiceResponse.builder()
                .id(service.getId())
                .title(service.getTitle())
                .slug(service.getSlug())
                .shortDescription(service.getShortDescription())
                .description(service.getDescription())
                .icon(service.getIcon())
                .popularTag(service.getPopularTag())
                .features(service.getFeatures() != null ? service.getFeatures() : new ArrayList<>())
                .buttonText(service.getButtonText())
                .buttonLink(service.getButtonLink())
                .displayOrder(service.getDisplayOrder())
                .isActive(service.isActive())
                .createdAt(service.getCreatedAt())
                .updatedAt(service.getUpdatedAt())
                .build();
    }

    public Service toService(ServiceRequest request) {
        if (request == null)
            return null;
        String slug = (request.getSlug() != null && !request.getSlug().trim().isEmpty())
                ? SlugUtils.toSlug(request.getSlug())
                : SlugUtils.toSlug(request.getTitle());

        return Service.builder()
                .title(request.getTitle())
                .slug(slug)
                .shortDescription(request.getShortDescription())
                .description(request.getDescription())
                .icon(request.getIcon())
                .popularTag(request.getPopularTag())
                .features(request.getFeatures() != null ? request.getFeatures() : new ArrayList<>())
                .buttonText(request.getButtonText())
                .buttonLink(request.getButtonLink())
                .displayOrder(request.getDisplayOrder())
                .isActive(request.getIsActive() == null || request.getIsActive())
                .build();
    }

    // PROJECT
    public ProjectResponse toProjectResponse(Project project) {
        if (project == null)
            return null;
        String name = project.getName() != null && !project.getName().trim().isEmpty()
                ? project.getName()
                : project.getTitle();
        String summary = project.getShortDescription() != null && !project.getShortDescription().trim().isEmpty()
                ? project.getShortDescription()
                : (project.getDescription() != null && project.getDescription().length() > 160
                    ? project.getDescription().substring(0, 157) + "..."
                    : project.getDescription());
        String img = project.getFeaturedImage() != null && !project.getFeaturedImage().trim().isEmpty()
                ? project.getFeaturedImage()
                : project.getImage();
        String live = project.getProjectUrl() != null && !project.getProjectUrl().trim().isEmpty()
                ? project.getProjectUrl()
                : project.getLiveUrl();

        return ProjectResponse.builder()
                .id(project.getId())
                .name(name)
                .title(name)
                .projectName(name)
                .slug(project.getSlug())
                .subtitle(project.getSubtitle())
                .clientLabel(project.getClientLabel())
                .shortDescription(summary)
                .summary(summary)
                .description(project.getDescription())
                .fullDescription(project.getDescription())
                .solution(project.getDescription())
                .category(project.getCategory())
                .projectType(project.getProjectType() != null ? project.getProjectType() : project.getCategory())
                .featuredImage(img)
                .image(img)
                .thumbnailUrl(img)
                .features(project.getFeatures() != null ? project.getFeatures() : new ArrayList<>())
                .technologies(project.getTechnologies() != null ? project.getTechnologies() : new ArrayList<>())
                .projectUrl(live)
                .liveUrl(live)
                .githubUrl(project.getGithubUrl())
                .caseStudyUrl(project.getCaseStudyUrl())
                .status(project.getStatus() != null ? project.getStatus() : "PUBLISHED")
                .featured(project.isFeatured())
                .displayOrder(project.getDisplayOrder())
                .seoTitle(project.getSeoTitle())
                .seoDescription(project.getSeoDescription())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    public Project toProject(ProjectRequest request) {
        if (request == null)
            return null;
        String slug = (request.getSlug() != null && !request.getSlug().trim().isEmpty())
                ? SlugUtils.toSlug(request.getSlug())
                : SlugUtils.toSlug(request.getName());

        String name = request.getName();
        String img = request.getFeaturedImage();
        String url = request.getProjectUrl();
        String desc = (request.getDescription() != null && !request.getDescription().trim().isEmpty())
                ? request.getDescription()
                : request.getShortDescription();

        return Project.builder()
                .name(name)
                .title(name)
                .slug(slug)
                .subtitle(request.getSubtitle())
                .clientLabel(request.getClientLabel())
                .shortDescription(request.getShortDescription())
                .description(desc)
                .category(request.getCategory() != null ? request.getCategory().toUpperCase() : "WEB_APPLICATIONS")
                .projectType(request.getProjectType())
                .featuredImage(img)
                .image(img)
                .features(request.getFeatures() != null ? request.getFeatures() : new ArrayList<>())
                .technologies(request.getTechnologies() != null ? request.getTechnologies() : new ArrayList<>())
                .projectUrl(url)
                .liveUrl(url)
                .githubUrl(request.getGithubUrl())
                .caseStudyUrl(request.getCaseStudyUrl())
                .status(request.getStatus() != null ? request.getStatus().toUpperCase() : "PUBLISHED")
                .featured(request.getFeatured() != null && request.getFeatured())
                .displayOrder(request.getDisplayOrder())
                .seoTitle(request.getSeoTitle())
                .seoDescription(request.getSeoDescription())
                .build();
    }

    // BLOG
    public BlogResponse toBlogResponse(Blog blog) {
        if (blog == null)
            return null;
        return BlogResponse.builder()
                .id(blog.getId())
                .title(blog.getTitle())
                .slug(blog.getSlug())
                .excerpt(blog.getExcerpt())
                .content(blog.getContent())
                .featuredImage(blog.getFeaturedImage())
                .category(blog.getCategory())
                .author(blog.getAuthor())
                .tags(blog.getTags() != null ? blog.getTags() : new ArrayList<>())
                .status(blog.getStatus())
                .publishedAt(blog.getPublishedAt())
                .readTime(SlugUtils.calculateReadingTime(blog.getContent()))
                .seoTitle(blog.getSeoTitle())
                .seoDescription(blog.getSeoDescription())
                .createdAt(blog.getCreatedAt())
                .updatedAt(blog.getUpdatedAt())
                .build();
    }

    public Blog toBlog(BlogRequest request) {
        if (request == null)
            return null;
        String slug = (request.getSlug() != null && !request.getSlug().trim().isEmpty())
                ? SlugUtils.toSlug(request.getSlug())
                : SlugUtils.toSlug(request.getTitle());

        String status = request.getStatus() != null ? request.getStatus().toUpperCase() : "DRAFT";
        Instant publishedAt = request.getPublishedAt();
        if ("PUBLISHED".equals(status) && publishedAt == null) {
            publishedAt = Instant.now();
        }

        return Blog.builder()
                .title(request.getTitle())
                .slug(slug)
                .excerpt(request.getExcerpt())
                .content(request.getContent())
                .featuredImage(request.getFeaturedImage())
                .category(request.getCategory())
                .author(request.getAuthor())
                .tags(request.getTags() != null ? request.getTags() : new ArrayList<>())
                .status(status)
                .publishedAt(publishedAt)
                .seoTitle(request.getSeoTitle())
                .seoDescription(request.getSeoDescription())
                .build();
    }

    // INQUIRY
    public ContactInquiryResponse toInquiryResponse(ContactInquiry inquiry) {
        if (inquiry == null)
            return null;
        return ContactInquiryResponse.builder()
                .id(inquiry.getId())
                .fullName(inquiry.getFullName())
                .email(inquiry.getEmail())
                .phone(inquiry.getPhone())
                .companyName(inquiry.getCompanyName())
                .service(inquiry.getService())
                .budget(inquiry.getBudget())
                .message(inquiry.getMessage())
                .status(inquiry.getStatus())
                .createdAt(inquiry.getCreatedAt())
                .updatedAt(inquiry.getUpdatedAt())
                .build();
    }

    public ContactInquiry toContactInquiry(ContactInquiryRequest request) {
        if (request == null)
            return null;
        return ContactInquiry.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .companyName(request.getCompanyName())
                .service(request.getService())
                .budget(request.getBudget())
                .message(request.getMessage())
                .status("NEW")
                .build();
    }

    // SITE SETTINGS
    public SiteSettingsResponse toSiteSettingsResponse(SiteSettings settings) {
        if (settings == null)
            return null;
        return SiteSettingsResponse.builder()
                .id(settings.getId())
                .contactEmail(settings.getContactEmail())
                .phone(settings.getPhone())
                .whatsapp(settings.getWhatsapp())
                .location(settings.getLocation())
                .address(settings.getAddress())
                .workingHours(settings.getWorkingHours())
                .footerDescription(settings.getFooterDescription())
                .copyrightText(settings.getCopyrightText())
                .linkedin(settings.getLinkedin())
                .github(settings.getGithub())
                .instagram(settings.getInstagram())
                .facebook(settings.getFacebook())
                .budgetOptions(settings.getBudgetOptions() != null && !settings.getBudgetOptions().isEmpty()
                        ? settings.getBudgetOptions()
                        : java.util.List.of("Under ₹25,000", "₹25,000 – ₹50,000", "₹50,000 – ₹1,00,000", "₹1,00,000+", "Not Sure Yet"))
                .updatedAt(settings.getUpdatedAt())
                .build();
    }

    public void updateSiteSettings(SiteSettings entity, SiteSettingsRequest request) {
        if (entity == null || request == null)
            return;
        if (request.getContactEmail() != null)
            entity.setContactEmail(request.getContactEmail());
        if (request.getPhone() != null)
            entity.setPhone(request.getPhone());
        if (request.getWhatsapp() != null)
            entity.setWhatsapp(request.getWhatsapp());
        if (request.getLocation() != null)
            entity.setLocation(request.getLocation());
        if (request.getAddress() != null)
            entity.setAddress(request.getAddress());
        if (request.getWorkingHours() != null)
            entity.setWorkingHours(request.getWorkingHours());
        if (request.getFooterDescription() != null)
            entity.setFooterDescription(request.getFooterDescription());
        if (request.getCopyrightText() != null)
            entity.setCopyrightText(request.getCopyrightText());
        if (request.getLinkedin() != null)
            entity.setLinkedin(request.getLinkedin());
        if (request.getGithub() != null)
            entity.setGithub(request.getGithub());
        if (request.getInstagram() != null)
            entity.setInstagram(request.getInstagram());
        if (request.getFacebook() != null)
            entity.setFacebook(request.getFacebook());
        if (request.getBudgetOptions() != null)
            entity.setBudgetOptions(request.getBudgetOptions());
    }

    // CASE STUDY
    public CaseStudyResponse toCaseStudyResponse(CaseStudy caseStudy) {
        if (caseStudy == null)
            return null;
        return CaseStudyResponse.builder()
                .id(caseStudy.getId())
                .title(caseStudy.getTitle())
                .slug(caseStudy.getSlug())
                .subtitle(caseStudy.getSubtitle())
                .projectReference(caseStudy.getProjectReference())
                .category(caseStudy.getCategory())
                .clientLabel(caseStudy.getClientLabel())
                .summary(caseStudy.getSummary())
                .problem(caseStudy.getProblem())
                .solution(caseStudy.getSolution())
                .features(caseStudy.getFeatures() != null ? caseStudy.getFeatures() : new ArrayList<>())
                .technologies(caseStudy.getTechnologies() != null ? caseStudy.getTechnologies() : new ArrayList<>())
                .featuredImage(caseStudy.getFeaturedImage())
                .liveUrl(caseStudy.getLiveUrl())
                .githubUrl(caseStudy.getGithubUrl())
                .status(caseStudy.getStatus() != null ? caseStudy.getStatus() : "PUBLISHED")
                .featured(caseStudy.isFeatured())
                .displayOrder(caseStudy.getDisplayOrder())
                .seoTitle(caseStudy.getSeoTitle())
                .seoDescription(caseStudy.getSeoDescription())
                .createdAt(caseStudy.getCreatedAt())
                .updatedAt(caseStudy.getUpdatedAt())
                .build();
    }

    public CaseStudy toCaseStudy(CaseStudyRequest request) {
        if (request == null)
            return null;
        String slug = (request.getSlug() != null && !request.getSlug().trim().isEmpty())
                ? SlugUtils.toSlug(request.getSlug())
                : SlugUtils.toSlug(request.getTitle());

        return CaseStudy.builder()
                .title(request.getTitle())
                .slug(slug)
                .subtitle(request.getSubtitle())
                .projectReference(request.getProjectReference())
                .category(request.getCategory() != null ? request.getCategory().toUpperCase() : "WEB_APPLICATIONS")
                .clientLabel(request.getClientLabel())
                .summary(request.getSummary())
                .problem(request.getProblem())
                .solution(request.getSolution())
                .features(request.getFeatures() != null ? request.getFeatures() : new ArrayList<>())
                .technologies(request.getTechnologies() != null ? request.getTechnologies() : new ArrayList<>())
                .featuredImage(request.getFeaturedImage())
                .liveUrl(request.getLiveUrl())
                .githubUrl(request.getGithubUrl())
                .status(request.getStatus() != null ? request.getStatus().toUpperCase() : "PUBLISHED")
                .featured(request.getFeatured() != null && request.getFeatured())
                .displayOrder(request.getDisplayOrder())
                .seoTitle(request.getSeoTitle())
                .seoDescription(request.getSeoDescription())
                .build();
    }
}
