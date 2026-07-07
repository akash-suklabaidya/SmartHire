package com.backend.smarthire.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "SmartHire Enterprise API",
                version = "1.0",
                description = "REST API documentation for the SmartHire AI Recruitment Platform.",
                contact = @Contact(name = "Akash Suklabaidya", email = "akash.suklabaidya.dev@gmail.com")
        ),
        // This tells Swagger to apply the security requirement to all endpoints by default
        security = @SecurityRequirement(name = "Bearer Authentication")
)

@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Enter your JWT token here to access protected endpoints."
)
public class SwaggerConfig {
    // The annotations do all the heavy lifting!
}