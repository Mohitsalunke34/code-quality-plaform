package com.trident.codequality.repository;

import com.trident.codequality.domain.ComplianceCheck;
import com.trident.codequality.domain.ComplianceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplianceCheckRepository extends JpaRepository<ComplianceCheck, Long> {

    long countByStatus(ComplianceStatus status);
}
