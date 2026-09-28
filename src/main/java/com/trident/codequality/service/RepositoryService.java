package com.trident.codequality.service;

import com.trident.codequality.domain.CodeRepository;
import com.trident.codequality.domain.User;
import com.trident.codequality.dto.repository.RegisterRepositoryRequest;
import com.trident.codequality.dto.repository.RepositoryResponse;
import com.trident.codequality.exception.DuplicateResourceException;
import com.trident.codequality.exception.ResourceNotFoundException;
import com.trident.codequality.mapper.RepositoryMapper;
import com.trident.codequality.repository.CodeRepositoryRepository;
import com.trident.codequality.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class RepositoryService {

    private final CodeRepositoryRepository repositoryRepository;
    private final UserRepository userRepository;
    private final RepositoryMapper repositoryMapper;

    @Transactional
    public RepositoryResponse register(RegisterRepositoryRequest request, String username) {
        if (repositoryRepository.existsByOwnerAndName(request.owner(), request.name())) {
            throw new DuplicateResourceException(
                    "Repository already registered: " + request.owner() + "/" + request.name());
        }
        User user = requireUser(username);
        String branch = StringUtils.hasText(request.defaultBranch()) ? request.defaultBranch() : "main";
        CodeRepository repository = CodeRepository.builder()
                .owner(request.owner())
                .name(request.name())
                .url("https://github.com/%s/%s".formatted(request.owner(), request.name()))
                .defaultBranch(branch)
                .description(request.description())
                .active(true)
                .registeredBy(user)
                .build();
        return repositoryMapper.toResponse(repositoryRepository.save(repository));
    }

    @Transactional(readOnly = true)
    public RepositoryResponse get(Long id) {
        return repositoryMapper.toResponse(requireRepository(id));
    }

    @Transactional(readOnly = true)
    public Page<RepositoryResponse> list(boolean activeOnly, Pageable pageable) {
        Page<CodeRepository> page = activeOnly
                ? repositoryRepository.findByActiveTrue(pageable)
                : repositoryRepository.findAll(pageable);
        return page.map(repositoryMapper::toResponse);
    }

    @Transactional
    public void deactivate(Long id) {
        CodeRepository repository = requireRepository(id);
        repository.setActive(false);
    }

    public CodeRepository requireRepository(Long id) {
        return repositoryRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Repository", id));
    }

    private User requireUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}
