package com.trident.codequality.repository;

import com.trident.codequality.domain.QualityScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface QualityScoreRepository extends JpaRepository<QualityScore, Long> {

    @Query("select avg(q.overallScore) from QualityScore q")
    Double averageOverallScore();
}
