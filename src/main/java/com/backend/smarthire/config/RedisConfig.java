package com.backend.smarthire.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class RedisConfig {
    // We can leave this class completely empty for now!
    // Just having the @EnableCaching annotation on a @Configuration class
    // is enough to activate Spring's Redis auto-configuration.
}
