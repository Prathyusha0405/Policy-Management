package com.hcl.chubb.policy.api.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FlagPolicyRequest(@NotNull UUID policyId, @NotNull Boolean flaggedForReview) {
}
