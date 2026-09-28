package com.trident.codequality.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "quality_scores")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scan_id", unique = true)
    private Scan scan;

    @Column(name = "overall_score", nullable = false)
    private double overallScore;

    @Column(nullable = false)
    private double maintainability;

    @Column(nullable = false)
    private double reliability;

    @Column(nullable = false)
    private double security;

    @Column(name = "coverage_estimate", nullable = false)
    private double coverageEstimate;

    @Column(nullable = false, length = 2)
    private String grade;
}
