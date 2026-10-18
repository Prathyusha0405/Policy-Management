package com.hcl.chubb.policy.application.usecase;

import com.hcl.chubb.policy.application.port.out.PolicyQueryPort;
import com.hcl.chubb.policy.domain.exception.PolicyNotFoundException;
import com.hcl.chubb.policy.domain.model.Policy;

import java.util.UUID;

public class GetPolicyByIdUseCase {

    private final PolicyQueryPort policyQueryPort;

    public GetPolicyByIdUseCase(PolicyQueryPort policyQueryPort) {
        this.policyQueryPort = policyQueryPort;
    }

    public Policy execute(UUID id) {
        return policyQueryPort.findById(id).orElseThrow(() -> new PolicyNotFoundException(id));
    }
}
