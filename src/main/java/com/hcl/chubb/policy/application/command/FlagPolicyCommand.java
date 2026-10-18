package com.hcl.chubb.policy.application.command;

import java.util.UUID;

public record FlagPolicyCommand(UUID policyId, boolean flaggedForReview) {
}
