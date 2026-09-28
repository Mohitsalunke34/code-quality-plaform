package com.trident.codequality.dto.scan;

import com.trident.codequality.domain.ScanStatus;

import java.time.Instant;

public record ScanSummaryResponse(
        Long id,
        Long repositoryId,
        String repositoryFullName,
        ScanStatus status,
        String branch,
        String commitSha,
        int filesScanned,
        int issueCount,
        Double overallScore,
        String grade,
        Instant startedAt,
        Instant completedAt
) {
}
