package com.hcl.chubb.policy.support;

import com.hcl.chubb.policy.domain.model.LineOfBusiness;
import com.hcl.chubb.policy.domain.model.Policy;
import com.hcl.chubb.policy.domain.model.PolicyStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public final class PolicyTestDataFactory {

    public static final UUID POLICY_ID = UUID.fromString("3f2b8c1e-5a47-4d1e-9f3a-2c6b7d8e9a10");
    public static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 2, 20, 9, 15);

    private PolicyTestDataFactory() {
    }

    public static Policy aPolicy() {
        return aPolicy(POLICY_ID, false);
    }

    public static Policy aPolicy(UUID id, boolean flaggedForReview) {
        return new Policy(
                id,
                "POL-SG-000123",
                "Wei Ling Tan",
                LineOfBusiness.HEALTH,
                PolicyStatus.ACTIVE,
                new BigDecimal("2450.75"),
                "SGD",
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2027, 2, 28),
                "Singapore",
                "Priya Nair",
                flaggedForReview,
                CREATED_AT,
                CREATED_AT);
    }
}
