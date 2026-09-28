package com.trident.codequality.dto.repository;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRepositoryRequest(
        @NotBlank @Size(max = 200) String owner,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 100) String defaultBranch,
        @Size(max = 1000) String description
) {
}
