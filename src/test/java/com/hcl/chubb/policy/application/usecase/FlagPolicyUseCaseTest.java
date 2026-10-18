package com.hcl.chubb.policy.application.usecase;

import com.hcl.chubb.policy.application.command.FlagPolicyCommand;
import com.hcl.chubb.policy.application.port.out.PolicyCommandPort;
import com.hcl.chubb.policy.application.port.out.PolicyQueryPort;
import com.hcl.chubb.policy.application.port.out.SummaryCachePort;
import com.hcl.chubb.policy.domain.exception.PolicyNotFoundException;
import com.hcl.chubb.policy.domain.model.Policy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static com.hcl.chubb.policy.support.PolicyTestDataFactory.POLICY_ID;
import static com.hcl.chubb.policy.support.PolicyTestDataFactory.aPolicy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlagPolicyUseCaseTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-18T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private PolicyQueryPort policyQueryPort;
    @Mock
    private PolicyCommandPort policyCommandPort;
    @Mock
    private SummaryCachePort summaryCachePort;

    private FlagPolicyUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FlagPolicyUseCase(policyQueryPort, policyCommandPort, summaryCachePort, CLOCK);
    }

    @Test
    void execute_whenFlagChanges_savesPolicyAndEvictsSummaryCache() {
        Policy policy = aPolicy();
        when(policyQueryPort.findById(POLICY_ID)).thenReturn(Optional.of(policy));
        when(policyCommandPort.save(policy)).thenReturn(policy);

        Policy result = useCase.execute(new FlagPolicyCommand(POLICY_ID, true));

        assertThat(result.isFlaggedForReview()).isTrue();
        assertThat(result.getUpdatedAt()).isEqualTo(LocalDateTime.of(2026, 10, 18, 10, 0));
        verify(policyCommandPort).save(policy);
        verify(summaryCachePort).evictSummary();
    }

    @Test
    void execute_whenFlagUnchanged_doesNotSaveOrEvict() {
        Policy policy = aPolicy();
        when(policyQueryPort.findById(POLICY_ID)).thenReturn(Optional.of(policy));

        Policy result = useCase.execute(new FlagPolicyCommand(POLICY_ID, false));

        assertThat(result).isSameAs(policy);
        verify(policyCommandPort, never()).save(any());
        verifyNoInteractions(summaryCachePort);
    }

    @Test
    void execute_whenPolicyMissing_throwsNotFoundWithoutSideEffects() {
        when(policyQueryPort.findById(POLICY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new FlagPolicyCommand(POLICY_ID, true)))
                .isInstanceOf(PolicyNotFoundException.class);

        verifyNoInteractions(policyCommandPort, summaryCachePort);
    }
}
