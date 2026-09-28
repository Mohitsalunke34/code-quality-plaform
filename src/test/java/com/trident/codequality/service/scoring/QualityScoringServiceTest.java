package com.trident.codequality.service.scoring;

import com.trident.codequality.domain.IssueSeverity;
import com.trident.codequality.domain.QualityScore;
import com.trident.codequality.service.ai.AiAnalysisResult.AiQuality;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class QualityScoringServiceTest {

    private final QualityScoringService service = new QualityScoringService();

    @Test
    void computesWeightedScoreAndGradeForCleanCode() {
        QualityScore score = service.compute(
                List.of(new AiQuality(95.0, 95.0, 95.0, 90.0)),
                Map.of());

        assertThat(score.getOverallScore()).isEqualTo(95.0);
        assertThat(score.getGrade()).isEqualTo("A");
        assertThat(score.getCoverageEstimate()).isEqualTo(90.0);
    }

    @Test
    void appliesSeverityPenaltiesToOverallScore() {
        QualityScore score = service.compute(
                List.of(new AiQuality(90.0, 90.0, 90.0, 80.0)),
                Map.of(IssueSeverity.CRITICAL, 2L, IssueSeverity.HIGH, 1L));

        // base weighted = 90; penalty = 2*15 + 1*8 = 38 -> 52 (grade F)
        assertThat(score.getOverallScore()).isEqualTo(52.0);
        assertThat(score.getGrade()).isEqualTo("F");
    }

    @Test
    void usesDefaultsWhenNoQualityDataProvided() {
        QualityScore score = service.compute(List.of(), Map.of());

        assertThat(score.getOverallScore()).isEqualTo(75.0);
        assertThat(score.getGrade()).isEqualTo("C");
    }
}
