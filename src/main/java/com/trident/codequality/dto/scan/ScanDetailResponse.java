package com.trident.codequality.dto.scan;

import com.trident.codequality.domain.ScanStatus;

import java.time.Instant;
import java.util.List;

public record ScanDetailResponse(
        Long id,
        Long repositoryId,
        String repositoryFullName,
        ScanStatus status,
        String branch,
        String commitSha,
        int filesScanned,
        String errorMessage,
        Instant startedAt,
        Instant completedAt,
        QualityScoreResponse qualityScore,
        List<CodeIssueResponse> issues,
        List<GeneratedTestResponse> generatedTests,
        List<ComplianceCheckResponse> complianceChecks
) {
}
