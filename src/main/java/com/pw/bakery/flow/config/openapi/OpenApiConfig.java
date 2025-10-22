package com.pw.bakery.flow.config.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI Configuration for Bakery Flow Manager
 * Configures Swagger UI and API documentation
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bakery Flow Manager API")
                        .description("REST API for Bakery Flow Management System\n\n" +
                                "## Authentication\n" +
                                "All endpoints (except `/api/auth/**`) require JWT Bearer token authentication.\n\n" +
                                "## User Roles\n" +
                                "- **ADMIN**: Full system access\n" +
                                "- **MANAGER**: Operations and inventory management\n" +
                                "- **BAKER**: Production workflow management\n" +
                                "- **CASHIER**: Order and payment handling\n" +
                                "- **INVENTORY**: Stock management")
                        .version("0.0.1-SNAPSHOT")
                        .contact(new Contact()
                                .name("Bakery Flow Manager Team")
                                .email("support@bakery-flow.com")
                                .url("https://bakery-flow.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer")
                .description("Enter JWT Bearer token (without 'Bearer ' prefix)");
    }
}
