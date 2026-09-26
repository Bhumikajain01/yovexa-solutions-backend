# Yovexa Solutions Backend

[![Java 17+](https://img.shields.io/badge/Java-17%2B-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot 3.3.4](https://img.shields.io/badge/Spring%20Boot-3.3.4-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-Atlas%20Ready-47A248?logo=mongodb&logoColor=white)](https://www.mongodb.com/)
[![JWT Auth](https://img.shields.io/badge/Auth-JWT%20Stateless-000000?logo=jsonwebtokens&logoColor=white)](https://jwt.io/)
[![Swagger OpenAPI](https://img.shields.io/badge/API%20Docs-Swagger%20OpenAPI-85EA2D?logo=swagger&logoColor=black)](http://localhost:8080/swagger-ui.html)

A production-ready standalone REST API backend for the **Yovexa Solutions** dynamic CMS, portfolio, and corporate website. Built with **Spring Boot 3.x**, **Spring Data MongoDB**, **Spring Security with JWT authentication**, and **Jakarta Bean Validation**.

---

## 🚀 Features

- **Layered Clean Architecture**: Controllers, Services, Repositories, DTOs, Mappers, and Centralized Exception Handling.
- **MongoDB Atlas Compatible**: Document collections for dynamic CMS content, inquiries, settings, and administrators.
- **Stateless JWT Admin Authentication**: BCrypt hashed passwords, token generation, claims extraction, and role-based route guards.
- **Public & Admin Route Separation**: Public read/inquiry endpoints isolated from protected `/api/admin/**` management endpoints.
- **Dynamic Content Singletons**: Auto-deactivation logic ensuring a single active Hero and About section at any time.
- **Search, Filter & Pagination**: Full pagination, case-insensitive keyword search, and category/status filtering for Blogs, Projects, and Inquiries.
- **Clean Database Architecture**: Zero default/seed/demo data on startup. Database starts empty; initial admin registers via `/api/auth/register` and content is created explicitly through admin APIs.
- **OpenAPI 3 / Swagger Documentation**: Interactive UI with Bearer Authentication support at `/swagger-ui.html`.
- **CORS Configured**: Ready for local React development (`http://localhost:5173`, `http://localhost:5174`) and customizable production origins.

---

## 🛠 Tech Stack

- **Language**: Java 17+ / Java 21
- **Framework**: Spring Boot 3.3.4
- **Security**: Spring Security 6.x + JJWT (0.12.6)
- **Database**: Spring Data MongoDB (Atlas compatible)
- **Validation**: Jakarta Bean Validation (`spring-boot-starter-validation`)
- **API Documentation**: SpringDoc OpenAPI 2.6.0 (Swagger UI)
- **Utilities**: Lombok, SLF4J

---

## 📂 Project Structure

```
yovexa-solutions-backend/
├── pom.xml
├── .env.example
├── .gitignore
├── README.md
└── src/
    ├── main/
    │   ├── java/com/yovexa/solutions/
    │   │   ├── YovexaSolutionsBackendApplication.java
    │   │   ├── config/
    │   │   │   ├── CorsConfig.java
    │   │   │   ├── MongoConfig.java
    │   │   │   ├── OpenApiConfig.java
    │   │   │   └── SecurityConfig.java
    │   │   ├── controller/
    │   │   │   ├── AuthController.java
    │   │   │   ├── HeroController.java
    │   │   │   ├── AboutController.java
    │   │   │   ├── ServiceController.java
    │   │   │   ├── ProcessController.java
    │   │   │   ├── ProjectController.java
    │   │   │   ├── BlogController.java
    │   │   │   ├── InquiryController.java
    │   │   │   ├── SiteSettingsController.java
    │   │   │   └── DashboardController.java
    │   │   ├── dto/
    │   │   │   ├── common/ (ApiResponse, PagedResponse)
    │   │   │   ├── auth/
    │   │   │   ├── hero/
    │   │   │   ├── about/
    │   │   │   ├── service/
    │   │   │   ├── process/
    │   │   │   ├── project/
    │   │   │   ├── blog/
    │   │   │   ├── inquiry/
    │   │   │   ├── settings/
    │   │   │   └── dashboard/
    │   │   ├── exception/
    │   │   │   ├── ApiException.java
    │   │   │   ├── ResourceNotFoundException.java
    │   │   │   ├── DuplicateResourceException.java
    │   │   │   ├── UnauthorizedException.java
    │   │   │   └── GlobalExceptionHandler.java
    │   │   ├── mapper/
    │   │   │   └── EntityMapper.java
    │   │   ├── model/
    │   │   │   ├── Admin.java
    │   │   │   ├── HeroSection.java
    │   │   │   ├── AboutSection.java
    │   │   │   ├── Service.java
    │   │   │   ├── ProcessStep.java
    │   │   │   ├── Project.java
    │   │   │   ├── Blog.java
    │   │   │   ├── ContactInquiry.java
    │   │   │   └── SiteSettings.java
    │   │   ├── repository/
    │   │   │   ├── AdminRepository.java
    │   │   │   ├── HeroSectionRepository.java
    │   │   │   ├── AboutSectionRepository.java
    │   │   │   ├── ServiceRepository.java
    │   │   │   ├── ProcessStepRepository.java
    │   │   │   ├── ProjectRepository.java
    │   │   │   ├── BlogRepository.java
    │   │   │   ├── ContactInquiryRepository.java
    │   │   │   └── SiteSettingsRepository.java
    │   │   ├── security/
    │   │   │   ├── CustomUserDetailsService.java
    │   │   │   ├── JwtAuthenticationEntryPoint.java
    │   │   │   ├── JwtAuthenticationFilter.java
    │   │   │   └── JwtService.java
    │   │   ├── service/
    │   │   │   ├── AuthService.java
    │   │   │   ├── HeroService.java
    │   │   │   ├── AboutService.java
    │   │   │   ├── ServicesService.java
    │   │   │   ├── ProcessService.java
    │   │   │   ├── ProjectService.java
    │   │   │   ├── BlogService.java
    │   │   │   ├── InquiryService.java
    │   │   │   ├── SiteSettingsService.java
    │   │   │   ├── DashboardService.java
    │   │   │   └── impl/
    │   │   └── util/
    │   │       ├── DatabaseDataInitializer.java
    │   │       └── SlugUtils.java
    │   └── resources/
    │       └── application.yml
    └── test/
        └── java/com/yovexa/solutions/
```

---

## ⚙️ Environment Variables

Create a `.env` file or export the following variables:

| Variable | Description | Default / Example |
|---|---|---|
| `SERVER_PORT` | HTTP port for backend server | `8080` |
| `MONGODB_URI` | MongoDB Connection URI (Local or Atlas) | `mongodb://localhost:27017/yovexa_admin` |
| `MONGODB_DATABASE` | MongoDB Database Name | `yovexa_admin` |
| `JWT_SECRET` | 256-bit secret string for HMAC signing | `404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970` |
| `JWT_EXPIRATION_MS` | Token validity duration in milliseconds | `86400000` (24h) |
| `ADMIN_NAME` | Initial admin account name | `Yovexa Admin` |
| `ADMIN_EMAIL` | Initial admin email address | `admin@yovexasolutions.com` |
| `ADMIN_PASSWORD` | Initial admin password (BCrypt hashed on startup) | `admin123` |
| `FRONTEND_URL` | Allowed CORS origins (comma-separated) | `http://localhost:5173,http://localhost:5174` |

---

## 🚦 Getting Started

### 1. Prerequisites
- **Java 17** or **Java 21** installed (`java -version`)
- **Maven 3.8+** installed (`mvn -version`)
- **MongoDB** running locally or a **MongoDB Atlas** cluster URI

### 2. Build the Application
```bash
mvn clean install
```

### 3. Run Locally
```bash
mvn spring-boot:run
```

The server will start on `http://localhost:8080`.

---

## 📖 API Documentation & Swagger UI

Once running, access the interactive OpenAPI documentation:

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

> **To access Admin APIs in Swagger UI**:
> 1. Call `POST /api/auth/login` to get a JWT token.
> 2. Click the green **Authorize** button at the top right in Swagger UI.
> 3. Paste the token into the `bearerAuth` field and click **Authorize**.

---

## 🔌 Frontend Integration Guide

The following table shows how each service in the `yovexa-solutions-frontend` maps to the backend REST endpoints:

| Frontend Service (`src/services/`) | Method | Backend REST Endpoint | Access |
|---|---|---|---|
| `authService.login()` | `POST` | `/api/auth/login` | Public |
| `authService.logout()` | `POST` | `/api/auth/logout` | Public |
| `heroService.getActiveHero()` | `GET` | `/api/hero/active` | Public |
| `heroService.getHeroes()` | `GET` | `/api/admin/hero` | Admin |
| `heroService.createHero()` | `POST` | `/api/admin/hero` | Admin |
| `heroService.updateHero()` | `PUT` | `/api/admin/hero/{id}` | Admin |
| `heroService.deleteHero()` | `DELETE` | `/api/admin/hero/{id}` | Admin |
| `aboutService.getActiveAbout()` | `GET` | `/api/about/active` | Public |
| `aboutService.getAboutSections()` | `GET` | `/api/admin/about` | Admin |
| `aboutService.createAbout()` | `POST` | `/api/admin/about` | Admin |
| `aboutService.updateAbout()` | `PUT` | `/api/admin/about/{id}` | Admin |
| `aboutService.deleteAbout()` | `DELETE` | `/api/admin/about/{id}` | Admin |
| `servicesService.getServices()` | `GET` | `/api/services` | Public |
| `servicesService.getServiceBySlug()` | `GET` | `/api/services/{slug}` | Public |
| `servicesService.getAllServices()` | `GET` | `/api/admin/services` | Admin |
| `servicesService.createService()` | `POST` | `/api/admin/services` | Admin |
| `servicesService.updateService()` | `PUT` | `/api/admin/services/{id}` | Admin |
| `servicesService.deleteService()` | `DELETE` | `/api/admin/services/{id}` | Admin |
| `processService.getProcessSteps()` | `GET` | `/api/process` | Public |
| `processService.getAllSteps()` | `GET` | `/api/admin/process` | Admin |
| `processService.createStep()` | `POST` | `/api/admin/process` | Admin |
| `processService.updateStep()` | `PUT` | `/api/admin/process/{id}` | Admin |
| `processService.deleteStep()` | `DELETE` | `/api/admin/process/{id}` | Admin |
| `projectService.getPublicProjects()` | `GET` | `/api/projects?category=&search=` | Public |
| `projectService.getProjectBySlug()` | `GET` | `/api/projects/{slug}` | Public |
| `projectService.getAdminProjects()` | `GET` | `/api/admin/projects?page=0&size=10` | Admin |
| `projectService.createProject()` | `POST` | `/api/admin/projects` | Admin |
| `projectService.updateProject()` | `PUT` | `/api/admin/projects/{id}` | Admin |
| `projectService.deleteProject()` | `DELETE` | `/api/admin/projects/{id}` | Admin |
| `blogService.getPublicBlogs()` | `GET` | `/api/blogs?category=&search=` | Public |
| `blogService.getBlogBySlug()` | `GET` | `/api/blogs/{slug}` | Public |
| `blogService.getAdminBlogs()` | `GET` | `/api/admin/blogs?page=0&size=10` | Admin |
| `blogService.createBlog()` | `POST` | `/api/admin/blogs` | Admin |
| `blogService.updateBlog()` | `PUT` | `/api/admin/blogs/{id}` | Admin |
| `blogService.deleteBlog()` | `DELETE` | `/api/admin/blogs/{id}` | Admin |
| `inquiryService.submitInquiry()` | `POST` | `/api/inquiries` | Public |
| `inquiryService.getAdminInquiries()` | `GET` | `/api/admin/inquiries?page=0&size=10` | Admin |
| `inquiryService.updateInquiryStatus()`| `PUT` | `/api/admin/inquiries/{id}` | Admin |
| `inquiryService.deleteInquiry()` | `DELETE` | `/api/admin/inquiries/{id}` | Admin |
| `contentService.getFooterContent()` | `GET` | `/api/site-settings` | Public |
| `contentService.updateFooter()` | `PUT` | `/api/admin/site-settings` | Admin |
| `dashboardService.getStats()` | `GET` | `/api/admin/dashboard` | Admin |

---

## 📡 Sample API Requests & Responses

### 1. Admin Login
**`POST /api/auth/login`**
```json
{
  "email": "admin@yovexasolutions.com",
  "password": "admin123"
}
```
**Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": "664b3f81e1a5392019...",
      "name": "Yovexa Admin",
      "email": "admin@yovexasolutions.com",
      "role": "ADMIN"
    }
  },
  "timestamp": "2026-09-21T11:55:00Z"
}
```

### 2. Submit Contact Inquiry
**`POST /api/inquiries`**
```json
{
  "fullName": "Jane Doe",
  "email": "jane@acmecorp.com",
  "phone": "+1 555 123 4567",
  "companyName": "Acme Corp",
  "service": "Custom Web Application",
  "budget": "$10k - $25k",
  "message": "We need a custom customer portal built with React and Spring Boot."
}
```
**Response (201 Created):**
```json
{
  "success": true,
  "message": "Thank you for reaching out! We will be in touch soon.",
  "data": {
    "id": "664b3f81e1a5392020...",
    "fullName": "Jane Doe",
    "email": "jane@acmecorp.com",
    "phone": "+1 555 123 4567",
    "companyName": "Acme Corp",
    "service": "Custom Web Application",
    "budget": "$10k - $25k",
    "message": "We need a custom customer portal built with React and Spring Boot.",
    "status": "NEW",
    "createdAt": "2026-09-21T11:55:00Z",
    "updatedAt": "2026-09-21T11:55:00Z"
  }
}
```

### 3. Admin Dashboard Overview
**`GET /api/admin/dashboard`** *(Requires Authorization: Bearer `<token>`)*

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "projects": 12,
    "publishedProjects": 10,
    "services": 6,
    "publishedBlogs": 8,
    "draftBlogs": 2,
    "inquiries": 15,
    "newInquiries": 4,
    "recentBlogs": [...],
    "recentProjects": [...],
    "recentInquiries": [...]
  }
}
```

---

## 🔒 Security & Architecture Details

- **Password Hashing**: BCrypt with default salt rounds.
- **JWT Authentication Filter**: Intercepts HTTP requests, extracts `Bearer <token>`, validates HMAC-SHA256 signature and expiration, and populates Spring's `SecurityContextHolder`.
- **Global Error Handling**: `@RestControllerAdvice` translates validation violations, resource not found exceptions, duplicate key conflicts, and unauthorized access attempts into consistent JSON errors.
- **Single Active Item Guarantee**: Activating a Hero or About section automatically deactivates all other records to ensure a seamless public website experience.
