package com.trident.codequality.web;

import com.trident.codequality.dto.repository.RegisterRepositoryRequest;
import com.trident.codequality.dto.repository.RepositoryResponse;
import com.trident.codequality.service.RepositoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/repositories")
@RequiredArgsConstructor
@Tag(name = "Repositories", description = "Register and manage code repositories")
public class RepositoryController {

    private final RepositoryService repositoryService;

    @PostMapping
    @Operation(summary = "Register a repository for analysis")
    public ResponseEntity<RepositoryResponse> register(@Valid @RequestBody RegisterRepositoryRequest request,
                                                       Authentication authentication) {
        RepositoryResponse response = repositoryService.register(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a repository by id")
    public ResponseEntity<RepositoryResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(repositoryService.get(id));
    }

    @GetMapping
    @Operation(summary = "List repositories")
    public ResponseEntity<Page<RepositoryResponse>> list(
            @RequestParam(defaultValue = "true") boolean activeOnly,
            Pageable pageable) {
        return ResponseEntity.ok(repositoryService.list(activeOnly, pageable));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a repository")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        repositoryService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
