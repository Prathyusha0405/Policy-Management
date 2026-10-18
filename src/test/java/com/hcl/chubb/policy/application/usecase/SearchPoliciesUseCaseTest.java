package com.hcl.chubb.policy.application.usecase;

import com.hcl.chubb.policy.application.port.out.PolicyQueryPort;
import com.hcl.chubb.policy.application.query.PageQuery;
import com.hcl.chubb.policy.application.query.PolicySearchQuery;
import com.hcl.chubb.policy.application.query.PolicySortField;
import com.hcl.chubb.policy.application.query.SortDirection;
import com.hcl.chubb.policy.application.result.PageResult;
import com.hcl.chubb.policy.domain.model.Policy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.hcl.chubb.policy.support.PolicyTestDataFactory.aPolicy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchPoliciesUseCaseTest {

    @Mock
    private PolicyQueryPort policyQueryPort;

    @InjectMocks
    private SearchPoliciesUseCase useCase;

    @Test
    void execute_delegatesQueryToPortAndReturnsItsResult() {
        PolicySearchQuery query = new PolicySearchQuery("tan", null, null, null, null, null,
                new PageQuery(0, 20, PolicySortField.EFFECTIVE_DATE, SortDirection.DESC));
        PageResult<Policy> expected = new PageResult<>(List.of(aPolicy()), 0, 20, 1, 1);
        when(policyQueryPort.search(query)).thenReturn(expected);

        PageResult<Policy> result = useCase.execute(query);

        assertThat(result).isSameAs(expected);
        verify(policyQueryPort).search(query);
    }
}
