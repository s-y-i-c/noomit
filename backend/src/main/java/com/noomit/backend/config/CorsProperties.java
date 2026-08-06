package com.noomit.backend.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "noomit.cors")
public record CorsProperties(List<String> allowedOrigins) {
    public String[] allowedOriginsArray() {
        return allowedOrigins.toArray(String[]::new);
    }
}
