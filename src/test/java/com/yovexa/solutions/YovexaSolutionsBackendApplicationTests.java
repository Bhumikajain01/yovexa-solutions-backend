package com.yovexa.solutions;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.yovexa.solutions.service.ProjectService;
import com.yovexa.solutions.service.CaseStudyService;
import com.yovexa.solutions.service.BlogService;
import com.yovexa.solutions.service.InquiryService;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class YovexaSolutionsBackendApplicationTests {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private CaseStudyService caseStudyService;

    @Autowired
    private BlogService blogService;

    @Autowired
    private InquiryService inquiryService;

    @Test
    void testAllAdminEndpoints() {
        System.out.println("Testing getAdminProjects...");
        var projects = projectService.getAdminProjects(null, null, null, 0, 100);
        assertNotNull(projects);
        System.out.println("Projects count: " + projects.getContent().size());

        System.out.println("Testing getAdminCaseStudies...");
        var caseStudies = caseStudyService.getAdminCaseStudies(null, null, null, 0, 100);
        assertNotNull(caseStudies);
        System.out.println("CaseStudies count: " + caseStudies.getContent().size());

        System.out.println("Testing getAdminBlogs...");
        var blogs = blogService.getAdminBlogs(null, null, null, 0, 100);
        assertNotNull(blogs);
        System.out.println("Blogs count: " + blogs.getContent().size());

        System.out.println("Testing getAdminInquiries...");
        var inquiries = inquiryService.getAdminInquiries(null, null, 0, 100);
        assertNotNull(inquiries);
        System.out.println("Inquiries count: " + inquiries.getContent().size());

        System.out.println("Testing getProjectCategories...");
        var projCategories = projectService.getProjectCategories();
        assertNotNull(projCategories);
        System.out.println("Project categories: " + projCategories);

        System.out.println("Testing getCaseStudyCategories...");
        var csCategories = caseStudyService.getCaseStudyCategories();
        assertNotNull(csCategories);
        System.out.println("Case study categories: " + csCategories);
    }
}
