package com.trident.codequality.service.github;

import java.util.List;

public interface GitHubService {

    /**
     * Resolves the latest commit SHA for the given branch.
     */
    String resolveHeadCommit(String owner, String repo, String branch);

    /**
     * Fetches analyzable source files from the repository tree at the given branch,
     * limited to a sensible number/size to keep AI cost bounded.
     */
    List<SourceFile> fetchSourceFiles(String owner, String repo, String branch, int maxFiles);
}
