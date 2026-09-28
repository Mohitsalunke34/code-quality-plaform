package com.trident.codequality.repository;

import com.trident.codequality.domain.CodeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodeRepositoryRepository extends JpaRepository<CodeRepository, Long> {

    boolean existsByOwnerAndName(String owner, String name);

    Optional<CodeRepository> findByOwnerAndName(String owner, String name);

    Page<CodeRepository> findByActiveTrue(Pageable pageable);

    long countByActiveTrue();
}
