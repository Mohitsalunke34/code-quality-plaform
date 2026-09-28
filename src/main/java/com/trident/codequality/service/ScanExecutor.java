package com.trident.codequality.service;

import com.trident.codequality.domain.CodeIssue;
import com.trident.codequality.domain.ComplianceCheck;
import com.trident.codequality.domain.ComplianceStatus;
import com.trident.codequality.domain.GeneratedTest;
import com.trident.codequality.domain.IssueSeverity;
import com.trident.codequality.domain.Scan;
import com.trident.codequality.domain.ScanStatus;
import com.trident.codequality.repository.ScanRepository;
import com.trident.codequality.service.ai.AiAnalysisResult;
import com.trident.codequality.service.ai.AiAnalysisService;
import com.trident.codequality.service.github.GitHubService;
import com.trident.codequality.service.github.SourceFile;
import com.trident.codequality.service.scoring.QualityScoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScanExecutor {

    private static final int MAX_FILES = 25;

    private final ScanRepository scanRepository;
    private final GitHubService gitHubService;
    private final AiAnalysisService aiAnalysisService;
    private final QualityScoringService qualityScoringService;

    @Async
    @Transactional
    public void execute(Long scanId) {
        Scan scan = scanRepository.findById(scanId).orElse(null);
        if (scan == null) {
            log.warn("Scan {} disappeared before execution", scanId);
            return;
        }
        scan.setStatus(ScanStatus.RUNNING);
        scan.setStartedAt(Instant.now());
        try {
            runAnalysis(scan);
            scan.setStatus(ScanStatus.COMPLETED);
        } catch (Exception ex) {
            log.error("Scan {} failed", scanId, ex);
            scan.setStatus(ScanStatus.FAILED);
            scan.setErrorMessage(truncate(ex.getMessage()));
        } finally {
            scan.setCompletedAt(Instant.now());
            scanRepository.save(scan);
        }
    }

    private void runAnalysis(Scan scan) {
        String owner = scan.getRepository().getOwner();
        String repo = scan.getRepository().getName();
        String branch = scan.getBranch();

        scan.setCommitSha(gitHubService.resolveHeadCommit(owner, repo, branch));
        List<SourceFile> files = gitHubService.fetchSourceFiles(owner, repo, branch, MAX_FILES);

        List<AiAnalysisResult.AiQuality> qualities = new ArrayList<>();
        Map<IssueSeverity, Long> severityCounts = new EnumMap<>(IssueSeverity.class);

        for (SourceFile file : files) {
            AiAnalysisResult result = aiAnalysisService.analyzeFile(file);
            applyIssues(scan, file, result, severityCounts);
            applyTests(scan, file, result);
            applyCompliance(scan, result);
            if (result.quality() != null) {
                qualities.add(result.quality());
            }
        }

        scan.setFilesScanned(files.size());
        scan.setQualityScore(qualityScoringService.compute(qualities, severityCounts));
    }

    private void applyIssues(Scan scan, SourceFile file, AiAnalysisResult result,
                             Map<IssueSeverity, Long> counts) {
        if (result.issues() == null) {
            return;
        }
        for (AiAnalysisResult.AiIssue issue : result.issues()) {
            IssueSeverity severity = parseSeverity(issue.severity());
            counts.merge(severity, 1L, Long::sum);
            scan.addIssue(CodeIssue.builder()
                    .filePath(file.path())
                    .lineNumber(issue.lineNumber())
                    .severity(severity)
                    .category(defaultText(issue.category(), "GENERAL"))
                    .title(defaultText(issue.title(), "Unspecified issue"))
                    .description(issue.description())
                    .suggestion(issue.suggestion())
                    .build());
        }
    }

    private void applyTests(Scan scan, SourceFile file, AiAnalysisResult result) {
        if (result.tests() == null) {
            return;
        }
        for (AiAnalysisResult.AiTest test : result.tests()) {
            if (!StringUtils.hasText(test.testCode())) {
                continue;
            }
            scan.addGeneratedTest(GeneratedTest.builder()
                    .sourceFilePath(file.path())
                    .testFilePath(defaultText(test.testFilePath(), deriveTestPath(file.path())))
                    .framework(defaultText(test.framework(), "unspecified"))
                    .testCode(test.testCode())
                    .description(test.description())
                    .build());
        }
    }

    private void applyCompliance(Scan scan, AiAnalysisResult result) {
        if (result.compliance() == null) {
            return;
        }
        for (AiAnalysisResult.AiCompliance c : result.compliance()) {
            scan.addComplianceCheck(ComplianceCheck.builder()
                    .standard(defaultText(c.standard(), "GENERAL"))
                    .rule(defaultText(c.rule(), "Unspecified rule"))
                    .status(parseComplianceStatus(c.status()))
                    .details(c.details())
                    .build());
        }
    }

    private IssueSeverity parseSeverity(String raw) {
        if (raw == null) {
            return IssueSeverity.MEDIUM;
        }
        try {
            return IssueSeverity.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return IssueSeverity.MEDIUM;
        }
    }

    private ComplianceStatus parseComplianceStatus(String raw) {
        if (raw == null) {
            return ComplianceStatus.NOT_APPLICABLE;
        }
        try {
            return ComplianceStatus.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return ComplianceStatus.NOT_APPLICABLE;
        }
    }

    private String deriveTestPath(String sourcePath) {
        int dot = sourcePath.lastIndexOf('.');
        if (dot < 0) {
            return sourcePath + "Test";
        }
        return sourcePath.substring(0, dot) + "Test" + sourcePath.substring(dot);
    }

    private String defaultText(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private String truncate(String message) {
        if (message == null) {
            return "Unknown error";
        }
        return message.length() > 2000 ? message.substring(0, 2000) : message;
    }
}
