package com.trident.codequality.mapper;

import com.trident.codequality.domain.CodeIssue;
import com.trident.codequality.domain.ComplianceCheck;
import com.trident.codequality.domain.GeneratedTest;
import com.trident.codequality.domain.QualityScore;
import com.trident.codequality.domain.Scan;
import com.trident.codequality.dto.scan.CodeIssueResponse;
import com.trident.codequality.dto.scan.ComplianceCheckResponse;
import com.trident.codequality.dto.scan.GeneratedTestResponse;
import com.trident.codequality.dto.scan.QualityScoreResponse;
import com.trident.codequality.dto.scan.ScanDetailResponse;
import com.trident.codequality.dto.scan.ScanSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ScanMapper {

    @Mapping(target = "repositoryId", source = "repository.id")
    @Mapping(target = "repositoryFullName",
            expression = "java(scan.getRepository().getOwner() + \"/\" + scan.getRepository().getName())")
    @Mapping(target = "issueCount",
            expression = "java(scan.getIssues() == null ? 0 : scan.getIssues().size())")
    @Mapping(target = "overallScore",
            expression = "java(scan.getQualityScore() == null ? null : scan.getQualityScore().getOverallScore())")
    @Mapping(target = "grade",
            expression = "java(scan.getQualityScore() == null ? null : scan.getQualityScore().getGrade())")
    ScanSummaryResponse toSummary(Scan scan);

    @Mapping(target = "repositoryId", source = "repository.id")
    @Mapping(target = "repositoryFullName",
            expression = "java(scan.getRepository().getOwner() + \"/\" + scan.getRepository().getName())")
    ScanDetailResponse toDetail(Scan scan);

    CodeIssueResponse toIssueResponse(CodeIssue issue);

    List<CodeIssueResponse> toIssueResponses(List<CodeIssue> issues);

    GeneratedTestResponse toTestResponse(GeneratedTest test);

    List<GeneratedTestResponse> toTestResponses(List<GeneratedTest> tests);

    QualityScoreResponse toQualityResponse(QualityScore score);

    ComplianceCheckResponse toComplianceResponse(ComplianceCheck check);

    List<ComplianceCheckResponse> toComplianceResponses(List<ComplianceCheck> checks);
}
