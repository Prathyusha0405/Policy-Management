package com.hcl.chubb.policy.domain.exception;

import java.util.UUID;

public class PolicyNotFoundException extends DomainException {

    public PolicyNotFoundException(UUID id) {
        super("Policy with id " + id + " was not found");
    }
}
