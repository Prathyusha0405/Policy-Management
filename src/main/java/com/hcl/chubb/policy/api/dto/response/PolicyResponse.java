package com.hcl.chubb.policy.api.dto.response;

import com.hcl.chubb.policy.domain.model.LineOfBusiness;
import com.hcl.chubb.policy.domain.model.PolicyStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PolicyResponse(
        UUID id,
        String policyNumber,
        String policyholderName,
        LineOfBusiness lineOfBusiness,
        PolicyStatus status,
        BigDecimal premiumAmount,
        String currency,
        LocalDate effectiveDate,
        LocalDate expiryDate,
        String region,
        String underwriter,
        boolean flaggedForReview,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
