package com.hcl.chubb.policy.api.mapper;

import com.hcl.chubb.policy.api.dto.request.FlagPolicyRequest;
import com.hcl.chubb.policy.api.dto.request.PolicySearchRequest;
import com.hcl.chubb.policy.api.dto.response.PolicyPageResponse;
import com.hcl.chubb.policy.api.dto.response.PolicyResponse;
import com.hcl.chubb.policy.api.dto.response.PolicySummaryResponse;
import com.hcl.chubb.policy.application.command.FlagPolicyCommand;
import com.hcl.chubb.policy.application.query.PolicySearchQuery;
import com.hcl.chubb.policy.application.query.PolicySortField;
import com.hcl.chubb.policy.application.query.SortDirection;
import com.hcl.chubb.policy.application.result.PageResult;
import com.hcl.chubb.policy.application.result.PolicySummaryResult;
import com.hcl.chubb.policy.application.result.PremiumByLineOfBusiness;
import com.hcl.chubb.policy.domain.model.LineOfBusiness;
import com.hcl.chubb.policy.domain.model.Policy;
import com.hcl.chubb.policy.domain.model.PolicyStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.hcl.chubb.policy.support.PolicyTestDataFactory.POLICY_ID;
import static com.hcl.chubb.policy.support.PolicyTestDataFactory.aPolicy;
import static org.assertj.core.api.Assertions.assertThat;

class PolicyApiMapperTest {

    private final PolicyApiMapper mapper = new PolicyApiMapper();

    private static PolicySearchRequest request(String sort) {
        return new PolicySearchRequest(2, 10, sort, "tan", PolicyStatus.ACTIVE, LineOfBusiness.HEALTH,
                "Singapore", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
    }

    @Test
    void toQuery_mapsFiltersAndPaging() {
        PolicySearchQuery query = mapper.toQuery(request("premiumAmount,desc"));

        assertThat(query.search()).isEqualTo("tan");
        assertThat(query.status()).isEqualTo(PolicyStatus.ACTIVE);
        assertThat(query.lineOfBusiness()).isEqualTo(LineOfBusiness.HEALTH);
        assertThat(query.region()).isEqualTo("Singapore");
        assertThat(query.effectiveDateFrom()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(query.effectiveDateTo()).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(query.page().page()).isEqualTo(2);
        assertThat(query.page().size()).isEqualTo(10);
        assertThat(query.page().sortBy()).isEqualTo(PolicySortField.PREMIUM_AMOUNT);
        assertThat(query.page().direction()).isEqualTo(SortDirection.DESC);
    }

    @Test
    void toQuery_defaultsDirectionToAscendingWhenOmitted() {
        PolicySearchQuery query = mapper.toQuery(request("region"));

        assertThat(query.page().sortBy()).isEqualTo(PolicySortField.REGION);
        assertThat(query.page().direction()).isEqualTo(SortDirection.ASC);
    }

    @Test
    void toQuery_usesDefaultsForEmptyRequest() {
        PolicySearchRequest empty = new PolicySearchRequest(null, null, null, null, null, null, null, null, null);

        PolicySearchQuery query = mapper.toQuery(empty);

        assertThat(query.page().page()).isZero();
        assertThat(query.page().size()).isEqualTo(20);
        assertThat(query.page().sortBy()).isEqualTo(PolicySortField.EFFECTIVE_DATE);
        assertThat(query.page().direction()).isEqualTo(SortDirection.DESC);
    }

    @Test
    void toCommand_mapsFlagRequest() {
        FlagPolicyCommand command = mapper.toCommand(new FlagPolicyRequest(POLICY_ID, true));

        assertThat(command.policyId()).isEqualTo(POLICY_ID);
        assertThat(command.flaggedForReview()).isTrue();
    }

    @Test
    void toResponse_mapsAllPolicyFields() {
        Policy policy = aPolicy();

        PolicyResponse response = mapper.toResponse(policy);

        assertThat(response).usingRecursiveComparison().isEqualTo(new PolicyResponse(
                policy.getId(), policy.getPolicyNumber(), policy.getPolicyholderName(), policy.getLineOfBusiness(),
                policy.getStatus(), policy.getPremiumAmount(), policy.getCurrency(), policy.getEffectiveDate(),
                policy.getExpiryDate(), policy.getRegion(), policy.getUnderwriter(), policy.isFlaggedForReview(),
                policy.getCreatedAt(), policy.getUpdatedAt()));
    }

    @Test
    void toPageResponse_mapsPagingAndEchoesSort() {
        PageResult<Policy> result = new PageResult<>(List.of(aPolicy()), 1, 5, 11, 3);

        PolicyPageResponse response = mapper.toPageResponse(result, "status,asc");

        assertThat(response.content()).hasSize(1);
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(5);
        assertThat(response.totalElements()).isEqualTo(11);
        assertThat(response.totalPages()).isEqualTo(3);
        assertThat(response.sort()).isEqualTo("status,asc");
    }

    @Test
    void toResponse_mapsSummary() {
        PolicySummaryResult result = new PolicySummaryResult(
                3, Map.of(PolicyStatus.ACTIVE, 3L),
                List.of(new PremiumByLineOfBusiness(LineOfBusiness.MOTOR, "AUD", new BigDecimal("100.00"))),
                1, 30);

        PolicySummaryResponse response = mapper.toResponse(result);

        assertThat(response.totalPolicies()).isEqualTo(3);
        assertThat(response.countByStatus()).containsEntry(PolicyStatus.ACTIVE, 3L);
        assertThat(response.premiumByLineOfBusiness()).singleElement().satisfies(p -> {
            assertThat(p.lineOfBusiness()).isEqualTo(LineOfBusiness.MOTOR);
            assertThat(p.currency()).isEqualTo("AUD");
            assertThat(p.totalPremium()).isEqualByComparingTo("100.00");
        });
        assertThat(response.expiringSoonCount()).isEqualTo(1);
        assertThat(response.expiringSoonDays()).isEqualTo(30);
    }
}
