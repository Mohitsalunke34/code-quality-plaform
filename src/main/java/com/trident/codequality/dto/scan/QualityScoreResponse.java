package com.trident.codequality.dto.scan;

public record QualityScoreResponse(
        double overallScore,
        double maintainability,
        double reliability,
        double security,
        double coverageEstimate,
        String grade
) {
}
