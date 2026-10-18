package com.hcl.chubb.policy.application.port.out;

import com.hcl.chubb.policy.application.result.PolicySummaryResult;

import java.time.LocalDate;

public interface PolicySummaryPort {

    /**
     * Aggregates the portfolio. Active policies expiring from {@code today} up to
     * {@code expiringSoonDays} days ahead (inclusive) count as expiring soon.
     */
    PolicySummaryResult summarize(LocalDate today, int expiringSoonDays);
}
