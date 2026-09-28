package com.trident.codequality.dto.scan;

public record GeneratedTestResponse(
        Long id,
        String sourceFilePath,
        String testFilePath,
        String framework,
        String testCode,
        String description
) {
}
