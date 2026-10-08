package com.yovexa.solutions.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

        @Value("${server.port:8081}")
        private String serverPort;

        @Bean
        public OpenAPI customOpenAPI() {
                final String securitySchemeName = "bearerAuth";

                return new OpenAPI()
                                .info(new Info()
                                                .title("Yovexa Solutions API")
                                                .description("REST API for Yovexa Solutions website, admin panel, authentication, CMS content management, projects, services, blogs, inquiries and site settings.")
                                                .version("1.0.0"))
                                .servers(List.of(
                                                new Server().url("/")
                                                                .description("Current Server (Production / Local)"),
                                                new Server().url("https://yovexa-solutions-backend1.vercel.app/:"
                                                                + serverPort).description("Local Development Server")))
                                .components(new Components()
                                                .addSecuritySchemes(securitySchemeName,
                                                                new SecurityScheme()
                                                                                .name(securitySchemeName)
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer")
                                                                                .bearerFormat("JWT")
                                                                                .description("Enter your JWT token to authorize protected admin endpoints.")));
        }
}
