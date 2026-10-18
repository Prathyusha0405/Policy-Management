package com.hcl.chubb.policy.application.usecase;

import com.hcl.chubb.policy.application.command.FlagPolicyCommand;
import com.hcl.chubb.policy.application.port.out.PolicyCommandPort;
import com.hcl.chubb.policy.application.port.out.PolicyQueryPort;
import com.hcl.chubb.policy.application.port.out.SummaryCachePort;
import com.hcl.chubb.policy.domain.exception.PolicyNotFoundException;
import com.hcl.chubb.policy.domain.model.Policy;

import java.time.Clock;
import java.time.LocalDateTime;

public class FlagPolicyUseCase {

    private final PolicyQueryPort policyQueryPort;
    private final PolicyCommandPort policyCommandPort;
    private final SummaryCachePort summaryCachePort;
    private final Clock clock;

    public FlagPolicyUseCase(PolicyQueryPort policyQueryPort, PolicyCommandPort policyCommandPort,
                             SummaryCachePort summaryCachePort, Clock clock) {
        this.policyQueryPort = policyQueryPort;
        this.policyCommandPort = policyCommandPort;
        this.summaryCachePort = summaryCachePort;
        this.clock = clock;
    }

    public Policy execute(FlagPolicyCommand command) {
        Policy policy = policyQueryPort.findById(command.policyId())
                .orElseThrow(() -> new PolicyNotFoundException(command.policyId()));

        if (!policy.flagForReview(command.flaggedForReview(), LocalDateTime.now(clock))) {
            return policy;
        }

        Policy saved = policyCommandPort.save(policy);
        summaryCachePort.evictSummary();
        return saved;
    }
}
