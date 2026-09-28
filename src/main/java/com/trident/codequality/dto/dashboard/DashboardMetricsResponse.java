package com.trident.codequality.dto.dashboard;

import java.util.Map;

public record DashboardMetricsResponse(
        long totalRepositories,
        long activeRepositories,
        long totalScans,
        long runningScans,
        long completedScans,
        long failedScans,
        double averageQualityScore,
        long totalIssues,
        Map<String, Long> issuesBySeverity,
        Map<String, Long> complianceByStatus
) {
}
