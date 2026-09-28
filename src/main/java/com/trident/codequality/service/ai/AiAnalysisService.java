package com.trident.codequality.service.ai;

import com.trident.codequality.service.github.SourceFile;

public interface AiAnalysisService {

    /**
     * Runs AI-powered analysis on a single source file: bug detection, quality
     * scoring dimensions, unit-test generation and compliance assessment.
     */
    AiAnalysisResult analyzeFile(SourceFile file);
}
