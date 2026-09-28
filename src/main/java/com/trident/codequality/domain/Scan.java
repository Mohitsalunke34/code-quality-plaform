package com.trident.codequality.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "scans")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@jakarta.persistence.EntityListeners(AuditingEntityListener.class)
public class Scan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "repository_id")
    private CodeRepository repository;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "triggered_by")
    private User triggeredBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScanStatus status;

    @Column(length = 100)
    private String branch;

    @Column(name = "commit_sha", length = 100)
    private String commitSha;

    @Column(name = "files_scanned", nullable = false)
    @Builder.Default
    private int filesScanned = 0;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToOne(mappedBy = "scan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private QualityScore qualityScore;

    @OneToMany(mappedBy = "scan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CodeIssue> issues = new ArrayList<>();

    @OneToMany(mappedBy = "scan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GeneratedTest> generatedTests = new ArrayList<>();

    @OneToMany(mappedBy = "scan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ComplianceCheck> complianceChecks = new ArrayList<>();

    public void addIssue(CodeIssue issue) {
        issue.setScan(this);
        this.issues.add(issue);
    }

    public void addGeneratedTest(GeneratedTest test) {
        test.setScan(this);
        this.generatedTests.add(test);
    }

    public void addComplianceCheck(ComplianceCheck check) {
        check.setScan(this);
        this.complianceChecks.add(check);
    }

    public void setQualityScore(QualityScore score) {
        if (score != null) {
            score.setScan(this);
        }
        this.qualityScore = score;
    }
}
