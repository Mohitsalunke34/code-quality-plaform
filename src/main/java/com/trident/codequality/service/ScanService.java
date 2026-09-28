package com.trident.codequality.service;

import com.trident.codequality.domain.CodeRepository;
import com.trident.codequality.domain.Scan;
import com.trident.codequality.domain.ScanStatus;
import com.trident.codequality.domain.User;
import com.trident.codequality.dto.scan.ScanDetailResponse;
import com.trident.codequality.dto.scan.ScanSummaryResponse;
import com.trident.codequality.exception.ResourceNotFoundException;
import com.trident.codequality.mapper.ScanMapper;
import com.trident.codequality.repository.ScanRepository;
import com.trident.codequality.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class ScanService {

    private final ScanRepository scanRepository;
    private final UserRepository userRepository;
    private final RepositoryService repositoryService;
    private final ScanExecutor scanExecutor;
    private final ScanMapper scanMapper;

    @Transactional
    public ScanSummaryResponse startScan(Long repositoryId, String username) {
        CodeRepository repository = repositoryService.requireRepository(repositoryId);
        if (!repository.isActive()) {
            throw new IllegalStateException("Repository is not active: " + repositoryId);
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Scan scan = Scan.builder()
                .repository(repository)
                .triggeredBy(user)
                .status(ScanStatus.PENDING)
                .branch(repository.getDefaultBranch())
                .build();
        Scan saved = scanRepository.save(scan);

        // Kick off asynchronous execution only after the PENDING row is committed.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                scanExecutor.execute(saved.getId());
            }
        });
        return scanMapper.toSummary(saved);
    }

    @Transactional(readOnly = true)
    public ScanDetailResponse getDetail(Long scanId) {
        Scan scan = scanRepository.findById(scanId)
                .orElseThrow(() -> ResourceNotFoundException.of("Scan", scanId));
        // Initialize lazy collections within the transaction before mapping.
        scan.getIssues().size();
        scan.getGeneratedTests().size();
        scan.getComplianceChecks().size();
        return scanMapper.toDetail(scan);
    }

    @Transactional(readOnly = true)
    public Page<ScanSummaryResponse> history(Long repositoryId, Pageable pageable) {
        repositoryService.requireRepository(repositoryId);
        Page<Scan> scans = scanRepository.findByRepositoryIdOrderByCreatedAtDesc(repositoryId, pageable);
        return scans.map(scanMapper::toSummary);
    }
}
