package com.trident.codequality.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "generated_tests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scan_id")
    private Scan scan;

    @Column(name = "source_file_path", nullable = false, length = 1000)
    private String sourceFilePath;

    @Column(name = "test_file_path", nullable = false, length = 1000)
    private String testFilePath;

    @Column(nullable = false, length = 50)
    private String framework;

    @Column(name = "test_code", nullable = false, columnDefinition = "TEXT")
    private String testCode;

    @Column(length = 1000)
    private String description;
}
