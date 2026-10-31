package com.library.management.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI libraryOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Library Management API")
                        .description("""
                                Half-baked library management backend for intern onboarding.

                                Fully implemented reference slices:
                                - POST /api/v1/users — public member registration
                                - POST /api/v1/auth/login — JWT login
                                - PUT /api/v1/books/{id} — secured book update (ADMIN, LIBRARIAN)

                                The Author resource is intentionally left unimplemented for interns.
                                """)
                        .version("0.1.0")
                        .contact(new Contact().name("Library Engineering").email("engineering@library.local")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
