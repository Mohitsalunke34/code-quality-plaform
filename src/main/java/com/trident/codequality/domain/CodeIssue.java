package com.trident.codequality.domain;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "code_issues")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scan_id")
    private Scan scan;

    @Column(name = "file_path", nullable = false, length = 1000)
    private String filePath;

    @Column(name = "line_number")
    private Integer lineNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IssueSeverity severity;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(length = 2000)
    private String suggestion;
}
