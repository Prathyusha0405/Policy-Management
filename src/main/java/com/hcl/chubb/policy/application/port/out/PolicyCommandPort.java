package com.hcl.chubb.policy.application.port.out;

import com.hcl.chubb.policy.domain.model.Policy;

public interface PolicyCommandPort {

    Policy save(Policy policy);
}
