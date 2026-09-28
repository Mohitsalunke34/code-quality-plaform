package com.trident.codequality.service.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.trident.codequality.config.WebClientConfig;
import com.trident.codequality.exception.ExternalServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class GitHubServiceImpl implements GitHubService {

    private static final Set<String> ANALYZABLE_EXTENSIONS = Set.of(
            "java", "kt", "js", "jsx", "ts", "tsx", "py", "go", "rb", "cs", "c", "cpp", "php", "rs", "scala");
    private static final long MAX_FILE_BYTES = 64_000L;

    private final WebClient github;

    public GitHubServiceImpl(@Qualifier(WebClientConfig.GITHUB_CLIENT) WebClient github) {
        this.github = github;
    }

    @Override
    public String resolveHeadCommit(String owner, String repo, String branch) {
        try {
            JsonNode node = github.get()
                    .uri("/repos/{owner}/{repo}/branches/{branch}", owner, repo, branch)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            if (node == null || node.path("commit").path("sha").isMissingNode()) {
                throw new ExternalServiceException("Unable to resolve HEAD commit for " + branch);
            }
            return node.path("commit").path("sha").asText();
        } catch (WebClientResponseException ex) {
            throw new ExternalServiceException(
                    "GitHub branch lookup failed (%d): %s".formatted(ex.getStatusCode().value(), ex.getMessage()), ex);
        }
    }

    @Override
    public List<SourceFile> fetchSourceFiles(String owner, String repo, String branch, int maxFiles) {
        try {
            String sha = resolveHeadCommit(owner, repo, branch);
            JsonNode tree = github.get()
                    .uri("/repos/{owner}/{repo}/git/trees/{sha}?recursive=1", owner, repo, sha)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (tree == null || !tree.has("tree")) {
                return List.of();
            }

            List<SourceFile> files = new ArrayList<>();
            for (JsonNode entry : tree.get("tree")) {
                if (files.size() >= maxFiles) {
                    break;
                }
                if (!"blob".equals(entry.path("type").asText())) {
                    continue;
                }
                String path = entry.path("path").asText();
                long size = entry.path("size").asLong(0);
                if (size == 0 || size > MAX_FILE_BYTES || !isAnalyzable(path)) {
                    continue;
                }
                fetchContent(owner, repo, path, branch).ifPresent(files::add);
            }
            log.info("Fetched {} analyzable files from {}/{}@{}", files.size(), owner, repo, branch);
            return files;
        } catch (WebClientResponseException ex) {
            throw new ExternalServiceException(
                    "GitHub tree fetch failed (%d): %s".formatted(ex.getStatusCode().value(), ex.getMessage()), ex);
        }
    }

    private java.util.Optional<SourceFile> fetchContent(String owner, String repo, String path, String branch) {
        try {
            JsonNode node = github.get()
                    .uri("/repos/{owner}/{repo}/contents/{path}?ref={ref}", owner, repo, path, branch)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            if (node == null || !"base64".equals(node.path("encoding").asText())) {
                return java.util.Optional.empty();
            }
            String encoded = node.path("content").asText().replaceAll("\\s", "");
            String content = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
            return java.util.Optional.of(new SourceFile(path, content));
        } catch (WebClientResponseException ex) {
            log.warn("Skipping file {} ({})", path, ex.getStatusCode());
            return java.util.Optional.empty();
        }
    }

    private boolean isAnalyzable(String path) {
        int dot = path.lastIndexOf('.');
        if (dot < 0 || dot == path.length() - 1) {
            return false;
        }
        return ANALYZABLE_EXTENSIONS.contains(path.substring(dot + 1).toLowerCase());
    }
}
