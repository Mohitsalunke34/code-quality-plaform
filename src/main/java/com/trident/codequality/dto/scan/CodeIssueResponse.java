package com.trident.codequality.dto.scan;

import com.trident.codequality.domain.IssueSeverity;

public record CodeIssueResponse(
        Long id,
        String filePath,
        Integer lineNumber,
        IssueSeverity severity,
        String category,
        String title,
        String description,
        String suggestion
) {
}
