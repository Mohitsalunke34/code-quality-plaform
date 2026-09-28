package com.trident.codequality.repository;

import com.trident.codequality.domain.GeneratedTest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneratedTestRepository extends JpaRepository<GeneratedTest, Long> {
}
