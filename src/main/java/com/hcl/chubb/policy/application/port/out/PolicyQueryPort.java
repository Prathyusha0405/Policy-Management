package com.hcl.chubb.policy.application.port.out;

import com.hcl.chubb.policy.application.query.PolicySearchQuery;
import com.hcl.chubb.policy.application.result.PageResult;
import com.hcl.chubb.policy.domain.model.Policy;

import java.util.Optional;
import java.util.UUID;

public interface PolicyQueryPort {

    PageResult<Policy> search(PolicySearchQuery query);

    Optional<Policy> findById(UUID id);
}
