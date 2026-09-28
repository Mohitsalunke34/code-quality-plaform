package com.trident.codequality.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai")
public record AiProperties(
        String provider,
        String baseUrl,
        String apiKey,
        String model,
        String azureDeployment,
        String azureApiVersion,
        int timeoutSeconds
) {
    public boolean isAzure() {
        return "azure".equalsIgnoreCase(provider);
    }
}
