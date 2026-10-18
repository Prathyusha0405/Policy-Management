package com.hcl.chubb.policy.application.usecase;

import com.hcl.chubb.policy.application.port.out.PolicyQueryPort;
import com.hcl.chubb.policy.domain.exception.PolicyNotFoundException;
import com.hcl.chubb.policy.domain.model.Policy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.hcl.chubb.policy.support.PolicyTestDataFactory.POLICY_ID;
import static com.hcl.chubb.policy.support.PolicyTestDataFactory.aPolicy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPolicyByIdUseCaseTest {

    @Mock
    private PolicyQueryPort policyQueryPort;

    @InjectMocks
    private GetPolicyByIdUseCase useCase;

    @Test
    void execute_returnsPolicyWhenFound() {
        Policy policy = aPolicy();
        when(policyQueryPort.findById(POLICY_ID)).thenReturn(Optional.of(policy));

        assertThat(useCase.execute(POLICY_ID)).isSameAs(policy);
    }

    @Test
    void execute_throwsNotFoundWhenMissing() {
        when(policyQueryPort.findById(POLICY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(POLICY_ID))
                .isInstanceOf(PolicyNotFoundException.class)
                .hasMessageContaining(POLICY_ID.toString());
    }
}
