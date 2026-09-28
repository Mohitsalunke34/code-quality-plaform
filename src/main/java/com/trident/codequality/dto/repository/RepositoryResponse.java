package com.trident.codequality.dto.repository;

import java.time.Instant;

public record RepositoryResponse(
        Long id,
        String owner,
        String name,
        String url,
        String defaultBranch,
        String description,
        boolean active,
        String registeredBy,
        Instant createdAt
) {
}
