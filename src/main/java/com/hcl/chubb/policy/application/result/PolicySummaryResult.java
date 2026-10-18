package com.hcl.chubb.policy.application.result;

import com.hcl.chubb.policy.domain.model.PolicyStatus;

import java.util.List;
import java.util.Map;

public record PolicySummaryResult(
        long totalPolicies,
        Map<PolicyStatus, Long> countByStatus,
        List<PremiumByLineOfBusiness> premiumByLineOfBusiness,
        long expiringSoonCount,
        int expiringSoonDays) {
}
