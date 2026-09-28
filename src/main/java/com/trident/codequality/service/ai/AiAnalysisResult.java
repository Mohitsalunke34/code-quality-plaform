package com.trident.codequality.service.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Provider-agnostic representation of a single file's AI analysis, parsed from
 * the model's JSON response. Fields default to empty to tolerate partial output.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AiAnalysisResult(
        List<AiIssue> issues,
        List<AiTest> tests,
        AiQuality quality,
        List<AiCompliance> compliance
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AiIssue(
            Integer lineNumber,
            String severity,
            String category,
            String title,
            String description,
            String suggestion
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AiTest(
            String testFilePath,
            String framework,
            String testCode,
            String description
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AiQuality(
            Double maintainability,
            Double reliability,
            Double security,
            Double coverageEstimate
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AiCompliance(
            String standard,
            String rule,
            String status,
            String details
    ) {
    }
}
