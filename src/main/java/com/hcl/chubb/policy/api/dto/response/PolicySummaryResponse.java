package com.hcl.chubb.policy.api.dto.response;

import com.hcl.chubb.policy.domain.model.LineOfBusiness;
import com.hcl.chubb.policy.domain.model.PolicyStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record PolicySummaryResponse(
        long totalPolicies,
        Map<PolicyStatus, Long> countByStatus,
        List<PremiumByLineOfBusinessResponse> premiumByLineOfBusiness,
        long expiringSoonCount,
        int expiringSoonDays) {

    public record PremiumByLineOfBusinessResponse(
            LineOfBusiness lineOfBusiness, String currency, BigDecimal totalPremium) {
    }
}
