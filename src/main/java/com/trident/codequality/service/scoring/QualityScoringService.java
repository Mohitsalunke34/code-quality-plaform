package com.trident.codequality.service.scoring;

import com.trident.codequality.domain.IssueSeverity;
import com.trident.codequality.domain.QualityScore;
import com.trident.codequality.service.ai.AiAnalysisResult.AiQuality;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Aggregates per-file AI quality dimensions and issue counts into a single,
 * deterministic {@link QualityScore}. Pure function of its inputs.
 */
@Service
public class QualityScoringService {

    private static final double DEFAULT_DIMENSION = 75.0;

    private static final Map<IssueSeverity, Double> SEVERITY_PENALTY = Map.of(
            IssueSeverity.INFO, 0.5,
            IssueSeverity.LOW, 1.5,
            IssueSeverity.MEDIUM, 4.0,
            IssueSeverity.HIGH, 8.0,
            IssueSeverity.CRITICAL, 15.0);

    public QualityScore compute(List<AiQuality> qualities, Map<IssueSeverity, Long> issueCounts) {
        double maintainability = average(qualities, AiQuality::maintainability);
        double reliability = average(qualities, AiQuality::reliability);
        double security = average(qualities, AiQuality::security);
        double coverage = average(qualities, AiQuality::coverageEstimate);

        double penalty = issueCounts.entrySet().stream()
                .mapToDouble(e -> SEVERITY_PENALTY.getOrDefault(e.getKey(), 0.0) * e.getValue())
                .sum();

        double weighted = maintainability * 0.30 + reliability * 0.35 + security * 0.35;
        double overall = clamp(weighted - penalty);

        return QualityScore.builder()
                .maintainability(round(maintainability))
                .reliability(round(reliability))
                .security(round(security))
                .coverageEstimate(round(coverage))
                .overallScore(round(overall))
                .grade(grade(overall))
                .build();
    }

    private double average(List<AiQuality> qualities, java.util.function.Function<AiQuality, Double> extractor) {
        return qualities.stream()
                .map(extractor)
                .filter(java.util.Objects::nonNull)
                .mapToDouble(this::clamp)
                .average()
                .orElse(DEFAULT_DIMENSION);
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(100.0, value));
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private String grade(double overall) {
        if (overall >= 90) return "A";
        if (overall >= 80) return "B";
        if (overall >= 70) return "C";
        if (overall >= 60) return "D";
        return "F";
    }
}
