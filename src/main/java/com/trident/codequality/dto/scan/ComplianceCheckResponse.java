package com.trident.codequality.dto.scan;

import com.trident.codequality.domain.ComplianceStatus;

public record ComplianceCheckResponse(
        Long id,
        String standard,
        String rule,
        ComplianceStatus status,
        String details
) {
}
