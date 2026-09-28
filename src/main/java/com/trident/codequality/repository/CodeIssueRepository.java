package com.trident.codequality.repository;

import com.trident.codequality.domain.CodeIssue;
import com.trident.codequality.domain.IssueSeverity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeIssueRepository extends JpaRepository<CodeIssue, Long> {

    long countBySeverity(IssueSeverity severity);
}
