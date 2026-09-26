package com.yovexa.solutions.service.impl;

import com.yovexa.solutions.dto.service.ServiceRequest;
import com.yovexa.solutions.dto.service.ServiceResponse;
import com.yovexa.solutions.exception.DuplicateResourceException;
import com.yovexa.solutions.exception.ResourceNotFoundException;
import com.yovexa.solutions.mapper.EntityMapper;
import com.yovexa.solutions.model.Service;
import com.yovexa.solutions.repository.ServiceRepository;
import com.yovexa.solutions.service.ServicesService;
import com.yovexa.solutions.util.SlugUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServicesServiceImpl implements ServicesService {

    private final ServiceRepository serviceRepository;
    private final EntityMapper mapper;

    @PostConstruct
    public void initDefaultServices() {
        if (serviceRepository.count() == 0) {
            List<Service> defaultServices = List.of(
                Service.builder()
                    .title("Website Development")
                    .slug("website-development")
                    .shortDescription("Modern, responsive websites designed to showcase your business, attract customers, and create a strong digital presence.")
                    .description("Custom business websites, landing pages, company websites, portfolio websites, and responsive web interfaces.")
                    .icon("Globe")
                    .popularTag("Business Essential")
                    .features(List.of(
                        "Responsive business websites",
                        "Modern UI/UX implementation",
                        "React.js & modern frontend development",
                        "SEO-friendly architecture",
                        "API & third-party integrations"
                    ))
                    .displayOrder(1)
                    .isActive(true)
                    .build(),
                Service.builder()
                    .title("App Development")
                    .slug("app-development")
                    .shortDescription("Scalable mobile applications built around your business requirements, user workflows, and product goals.")
                    .description("Custom mobile applications, business apps, customer-facing apps, dashboards, and API-connected application experiences.")
                    .icon("Smartphone")
                    .popularTag("Mobile Solutions")
                    .features(List.of(
                        "Cross-platform application development",
                        "Custom UI/UX",
                        "API & backend integration",
                        "Authentication & user management"
                    ))
                    .displayOrder(2)
                    .isActive(true)
                    .build(),
                Service.builder()
                    .title("Custom Software")
                    .slug("custom-software")
                    .shortDescription("Tailored enterprise and business software solutions engineered to automate operations and solve complex challenges.")
                    .description("Bespoke internal tools, automation pipelines, management systems, and specialized business logic.")
                    .icon("Code2")
                    .features(List.of(
                        "Custom business logic implementation",
                        "Workflow automation & tooling",
                        "High performance & reliable codebase",
                        "Scalable architecture"
                    ))
                    .displayOrder(3)
                    .isActive(true)
                    .build(),
                Service.builder()
                    .title("E-Commerce Solutions")
                    .slug("ecommerce-solutions")
                    .shortDescription("High-converting digital storefronts and online commerce platforms with secure payment gateways.")
                    .description("Full e-commerce architectures, product catalogs, shopping carts, order tracking, and checkout flows.")
                    .icon("Store")
                    .features(List.of(
                        "Custom product catalogs & cart flows",
                        "Payment gateway integrations",
                        "Inventory & order management",
                        "Speed & conversion optimization"
                    ))
                    .displayOrder(4)
                    .isActive(true)
                    .build(),
                Service.builder()
                    .title("API & Backend Development")
                    .slug("api-backend-development")
                    .shortDescription("Robust, high-throughput backend services and secure RESTful APIs designed for scalability.")
                    .description("Secure RESTful services, architecture modeling, microservices, and third-party API orchestrations.")
                    .icon("Server")
                    .features(List.of(
                        "Secure RESTful APIs",
                        "Architecture modeling & performance",
                        "JWT & authentication mechanisms",
                        "Scalable microservices & endpoints"
                    ))
                    .displayOrder(5)
                    .isActive(true)
                    .build()
            );
            serviceRepository.saveAll(defaultServices);
        }
    }

    @Override
    public List<ServiceResponse> getAllServices() {
        if (serviceRepository.count() == 0) {
            initDefaultServices();
        }
        return serviceRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(mapper::toServiceResponse)
                .toList();
    }

    @Override
    public List<ServiceResponse> getActiveServices() {
        if (serviceRepository.count() == 0) {
            initDefaultServices();
        }
        return serviceRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(mapper::toServiceResponse)
                .toList();
    }

    @Override
    public ServiceResponse getServiceById(String id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", id));
        return mapper.toServiceResponse(service);
    }

    @Override
    public ServiceResponse getServiceBySlug(String slug) {
        Service service = serviceRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Service", "slug", slug));
        return mapper.toServiceResponse(service);
    }

    @Override
    public ServiceResponse createService(ServiceRequest request) {
        Service service = mapper.toService(request);

        if (serviceRepository.existsBySlug(service.getSlug())) {
            service.setSlug(service.getSlug() + "-" + System.currentTimeMillis());
        }

        Service saved = serviceRepository.save(service);
        return mapper.toServiceResponse(saved);
    }

    @Override
    public ServiceResponse updateService(String id, ServiceRequest request) {
        Service existing = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", id));

        existing.setTitle(request.getTitle());

        String targetSlug = (request.getSlug() != null && !request.getSlug().trim().isEmpty())
                ? SlugUtils.toSlug(request.getSlug())
                : existing.getSlug();

        if (!existing.getSlug().equals(targetSlug) && serviceRepository.existsBySlug(targetSlug)) {
            throw new DuplicateResourceException("Service", "slug", targetSlug);
        }
        existing.setSlug(targetSlug);

        existing.setShortDescription(request.getShortDescription());
        existing.setDescription(request.getDescription());
        existing.setIcon(request.getIcon());
        existing.setPopularTag(request.getPopularTag());
        existing.setFeatures(request.getFeatures());
        existing.setButtonText(request.getButtonText());
        existing.setButtonLink(request.getButtonLink());
        existing.setDisplayOrder(request.getDisplayOrder());

        if (request.getIsActive() != null) {
            existing.setActive(request.getIsActive());
        }

        Service saved = serviceRepository.save(existing);
        return mapper.toServiceResponse(saved);
    }

    @Override
    public void deleteService(String id) {
        if (!serviceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Service", "id", id);
        }
        serviceRepository.deleteById(id);
    }
}
