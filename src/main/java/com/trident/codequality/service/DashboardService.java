package com.trident.codequality.service;

import com.trident.codequality.domain.ComplianceStatus;
import com.trident.codequality.domain.IssueSeverity;
import com.trident.codequality.domain.ScanStatus;
import com.trident.codequality.dto.dashboard.DashboardMetricsResponse;
import com.trident.codequality.repository.CodeIssueRepository;
import com.trident.codequality.repository.CodeRepositoryRepository;
import com.trident.codequality.repository.ComplianceCheckRepository;
import com.trident.codequality.repository.QualityScoreRepository;
import com.trident.codequality.repository.ScanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CodeRepositoryRepository repositoryRepository;
    private final ScanRepository scanRepository;
    private final CodeIssueRepository codeIssueRepository;
    private final ComplianceCheckRepository complianceCheckRepository;
    private final QualityScoreRepository qualityScoreRepository;

    @Transactional(readOnly = true)
    public DashboardMetricsResponse metrics() {
        Map<String, Long> issuesBySeverity = new LinkedHashMap<>();
        long totalIssues = 0;
        for (IssueSeverity severity : IssueSeverity.values()) {
            long count = codeIssueRepository.countBySeverity(severity);
            issuesBySeverity.put(severity.name(), count);
            totalIssues += count;
        }

        Map<String, Long> complianceByStatus = new LinkedHashMap<>();
        for (ComplianceStatus status : ComplianceStatus.values()) {
            complianceByStatus.put(status.name(), complianceCheckRepository.countByStatus(status));
        }

        Double avg = qualityScoreRepository.averageOverallScore();

        return new DashboardMetricsResponse(
                repositoryRepository.count(),
                repositoryRepository.countByActiveTrue(),
                scanRepository.count(),
                scanRepository.countByStatus(ScanStatus.RUNNING),
                scanRepository.countByStatus(ScanStatus.COMPLETED),
                scanRepository.countByStatus(ScanStatus.FAILED),
                avg == null ? 0.0 : Math.round(avg * 10.0) / 10.0,
                totalIssues,
                issuesBySeverity,
                complianceByStatus);
    }
}
