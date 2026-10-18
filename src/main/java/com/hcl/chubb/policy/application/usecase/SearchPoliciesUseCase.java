package com.hcl.chubb.policy.application.usecase;

import com.hcl.chubb.policy.application.port.out.PolicyQueryPort;
import com.hcl.chubb.policy.application.query.PolicySearchQuery;
import com.hcl.chubb.policy.application.result.PageResult;
import com.hcl.chubb.policy.domain.model.Policy;

public class SearchPoliciesUseCase {

    private final PolicyQueryPort policyQueryPort;

    public SearchPoliciesUseCase(PolicyQueryPort policyQueryPort) {
        this.policyQueryPort = policyQueryPort;
    }

    public PageResult<Policy> execute(PolicySearchQuery query) {
        return policyQueryPort.search(query);
    }
}
