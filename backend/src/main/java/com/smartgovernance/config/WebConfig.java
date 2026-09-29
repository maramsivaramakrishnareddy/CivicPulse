package com.smartgovernance.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web Configuration for CORS and other web settings.
 * Enables cross-origin requests from the React frontend running on different ports.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:5173",      // Vite dev server
                        "http://127.0.0.1:5173",      // Localhost alias
                        "http://0.0.0.0:5173",        // All interfaces
                        "http://localhost:5174",      // Alternate Vite port
                        "http://127.0.0.1:5174",      // Alternate localhost alias
                        "http://localhost:8085",      // Backend itself
                        "http://127.0.0.1:8085"       // Localhost alias
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
