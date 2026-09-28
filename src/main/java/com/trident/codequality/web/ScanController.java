package com.trident.codequality.web;

import com.trident.codequality.dto.scan.ScanDetailResponse;
import com.trident.codequality.dto.scan.ScanSummaryResponse;
import com.trident.codequality.service.ScanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Scans", description = "Trigger scans and read results and history")
public class ScanController {

    private final ScanService scanService;

    @PostMapping("/repositories/{repositoryId}/scans")
    @Operation(summary = "Trigger a new scan for a repository")
    public ResponseEntity<ScanSummaryResponse> startScan(@PathVariable Long repositoryId,
                                                          Authentication authentication) {
        ScanSummaryResponse response = scanService.startScan(repositoryId, authentication.getName());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/repositories/{repositoryId}/scans")
    @Operation(summary = "Get scan history for a repository")
    public ResponseEntity<Page<ScanSummaryResponse>> history(@PathVariable Long repositoryId,
                                                             Pageable pageable) {
        return ResponseEntity.ok(scanService.history(repositoryId, pageable));
    }

    @GetMapping("/scans/{scanId}")
    @Operation(summary = "Get full scan results including issues, tests and compliance")
    public ResponseEntity<ScanDetailResponse> detail(@PathVariable Long scanId) {
        return ResponseEntity.ok(scanService.getDetail(scanId));
    }
}
