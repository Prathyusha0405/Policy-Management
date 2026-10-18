package com.hcl.chubb.policy.application.usecase;

import com.hcl.chubb.policy.application.port.out.PolicySummaryPort;
import com.hcl.chubb.policy.application.result.PolicySummaryResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPolicySummaryUseCaseTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-18T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private PolicySummaryPort policySummaryPort;

    @Test
    void execute_requestsSummaryForTodayAndConfiguredWindow() {
        PolicySummaryResult expected = new PolicySummaryResult(0, Map.of(), List.of(), 0, 45);
        LocalDate today = LocalDate.of(2026, 10, 18);
        when(policySummaryPort.summarize(today, 45)).thenReturn(expected);

        PolicySummaryResult result = new GetPolicySummaryUseCase(policySummaryPort, CLOCK, 45).execute();

        assertThat(result).isSameAs(expected);
        verify(policySummaryPort).summarize(today, 45);
    }
}
