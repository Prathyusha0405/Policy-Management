package com.hcl.chubb.policy.application.usecase;

import com.hcl.chubb.policy.application.port.out.PolicySummaryPort;
import com.hcl.chubb.policy.application.result.PolicySummaryResult;

import java.time.Clock;
import java.time.LocalDate;

public class GetPolicySummaryUseCase {

    private final PolicySummaryPort policySummaryPort;
    private final Clock clock;
    private final int expiringSoonDays;

    public GetPolicySummaryUseCase(PolicySummaryPort policySummaryPort, Clock clock, int expiringSoonDays) {
        this.policySummaryPort = policySummaryPort;
        this.clock = clock;
        this.expiringSoonDays = expiringSoonDays;
    }

    public PolicySummaryResult execute() {
        return policySummaryPort.summarize(LocalDate.now(clock), expiringSoonDays);
    }
}
