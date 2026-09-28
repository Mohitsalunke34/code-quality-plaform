package com.trident.codequality.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "github")
public record GitHubProperties(
        String apiBaseUrl,
        String token
) {
}
