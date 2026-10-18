package com.hcl.chubb.policy.application.query;

import com.hcl.chubb.policy.domain.model.LineOfBusiness;
import com.hcl.chubb.policy.domain.model.PolicyStatus;

import java.time.LocalDate;

public record PolicySearchQuery(
        String search,
        PolicyStatus status,
        LineOfBusiness lineOfBusiness,
        String region,
        LocalDate effectiveDateFrom,
        LocalDate effectiveDateTo,
        PageQuery page) {
}
