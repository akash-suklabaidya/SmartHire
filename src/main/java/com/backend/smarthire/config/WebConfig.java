package com.backend.smarthire.config;

import com.backend.smarthire.interceptor.RateLimitInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    public WebConfig(RateLimitInterceptor rateLimitInterceptor) {
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Apply this rate limiter to specific, expensive endpoints.
        // We do not want to rate limit basic things like viewing the homepage,
        // but we absolutely want to protect the AI-heavy routes.

        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns(
                        "/api/profiles/upload-resume", // Protect the PDF parsing / Embedding
                        "/api/candidates/**"           // Protect the LLM Question / Email generation
                );
    }

}
