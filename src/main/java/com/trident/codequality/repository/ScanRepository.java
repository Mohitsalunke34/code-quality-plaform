package com.trident.codequality.repository;

import com.trident.codequality.domain.Scan;
import com.trident.codequality.domain.ScanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ScanRepository extends JpaRepository<Scan, Long> {

    Page<Scan> findByRepositoryIdOrderByCreatedAtDesc(Long repositoryId, Pageable pageable);

    long countByStatus(ScanStatus status);

    @Query("""
            select s from Scan s
            left join fetch s.qualityScore
            where s.id = :id
            """)
    Optional<Scan> findDetailedById(Long id);

    Optional<Scan> findTopByRepositoryIdAndStatusOrderByCompletedAtDesc(Long repositoryId, ScanStatus status);
}
