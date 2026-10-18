package com.hcl.chubb.policy.api.controller;

import com.hcl.chubb.policy.api.mapper.PolicyApiMapper;
import com.hcl.chubb.policy.application.command.FlagPolicyCommand;
import com.hcl.chubb.policy.application.query.PolicySearchQuery;
import com.hcl.chubb.policy.application.query.PolicySortField;
import com.hcl.chubb.policy.application.query.SortDirection;
import com.hcl.chubb.policy.application.result.PageResult;
import com.hcl.chubb.policy.application.result.PolicySummaryResult;
import com.hcl.chubb.policy.application.result.PremiumByLineOfBusiness;
import com.hcl.chubb.policy.application.usecase.FlagPolicyUseCase;
import com.hcl.chubb.policy.application.usecase.GetPolicyByIdUseCase;
import com.hcl.chubb.policy.application.usecase.GetPolicySummaryUseCase;
import com.hcl.chubb.policy.application.usecase.SearchPoliciesUseCase;
import com.hcl.chubb.policy.domain.exception.PolicyNotFoundException;
import com.hcl.chubb.policy.domain.model.LineOfBusiness;
import com.hcl.chubb.policy.domain.model.PolicyStatus;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.hcl.chubb.policy.support.PolicyTestDataFactory.POLICY_ID;
import static com.hcl.chubb.policy.support.PolicyTestDataFactory.aPolicy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PolicyController.class)
@Import({PolicyApiMapper.class, PolicyControllerTest.FixedClockConfig.class})
class PolicyControllerTest {

    private static final String BASE = "/api/v1/policies";

    @TestConfiguration
    static class FixedClockConfig {
        @Bean
        Clock clock() {
            return Clock.fixed(Instant.parse("2026-10-18T10:00:00Z"), ZoneOffset.UTC);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SearchPoliciesUseCase searchPoliciesUseCase;
    @MockitoBean
    private GetPolicyByIdUseCase getPolicyByIdUseCase;
    @MockitoBean
    private FlagPolicyUseCase flagPolicyUseCase;
    @MockitoBean
    private GetPolicySummaryUseCase getPolicySummaryUseCase;

    // ---- GET /api/v1/policies

    @Test
    void search_withoutParameters_usesDefaultsAndReturnsPage() throws Exception {
        when(searchPoliciesUseCase.execute(any()))
                .thenReturn(new PageResult<>(List.of(aPolicy()), 0, 20, 1, 1));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-Id"))
                .andExpect(jsonPath("$.content[0].policyNumber").value("POL-SG-000123"))
                .andExpect(jsonPath("$.content[0].premiumAmount").value(2450.75))
                .andExpect(jsonPath("$.content[0].effectiveDate").value("2026-03-01"))
                .andExpect(jsonPath("$.content[0].flaggedForReview").value(false))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.sort").value("effectiveDate,desc"));

        ArgumentCaptor<PolicySearchQuery> captor = ArgumentCaptor.forClass(PolicySearchQuery.class);
        verify(searchPoliciesUseCase).execute(captor.capture());
        PolicySearchQuery query = captor.getValue();
        assertThat(query.page().page()).isZero();
        assertThat(query.page().size()).isEqualTo(20);
        assertThat(query.page().sortBy()).isEqualTo(PolicySortField.EFFECTIVE_DATE);
        assertThat(query.page().direction()).isEqualTo(SortDirection.DESC);
        assertThat(query.status()).isNull();
    }

    @Test
    void search_bindsAllFiltersPagingAndSorting() throws Exception {
        when(searchPoliciesUseCase.execute(any())).thenReturn(new PageResult<>(List.of(), 2, 5, 0, 0));

        mockMvc.perform(get(BASE)
                        .param("page", "2").param("size", "5").param("sort", "premiumAmount,asc")
                        .param("search", "tan").param("status", "ACTIVE").param("lineOfBusiness", "HEALTH")
                        .param("region", "Singapore")
                        .param("effectiveDateFrom", "2026-01-01").param("effectiveDateTo", "2026-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sort").value("premiumAmount,asc"));

        ArgumentCaptor<PolicySearchQuery> captor = ArgumentCaptor.forClass(PolicySearchQuery.class);
        verify(searchPoliciesUseCase).execute(captor.capture());
        PolicySearchQuery query = captor.getValue();
        assertThat(query.search()).isEqualTo("tan");
        assertThat(query.status()).isEqualTo(PolicyStatus.ACTIVE);
        assertThat(query.lineOfBusiness()).isEqualTo(LineOfBusiness.HEALTH);
        assertThat(query.region()).isEqualTo("Singapore");
        assertThat(query.effectiveDateFrom()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(query.effectiveDateTo()).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(query.page().page()).isEqualTo(2);
        assertThat(query.page().size()).isEqualTo(5);
        assertThat(query.page().sortBy()).isEqualTo(PolicySortField.PREMIUM_AMOUNT);
        assertThat(query.page().direction()).isEqualTo(SortDirection.ASC);
    }

    @Test
    void search_withInvalidParameters_returnsProblemDetailListingEveryViolation() throws Exception {
        mockMvc.perform(get(BASE).param("size", "500").param("page", "-1").param("sort", "bogus"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.requestId").exists())
                .andExpect(jsonPath("$.errors[?(@.field=='size')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field=='page')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field=='sort')]").exists());
    }

    @Test
    void search_withReversedDateRange_isRejected() throws Exception {
        mockMvc.perform(get(BASE).param("effectiveDateFrom", "2026-05-01").param("effectiveDateTo", "2026-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].message").value("effectiveDateFrom must be on or before effectiveDateTo"));
    }

    @Test
    void search_withUnknownEnumValue_isRejectedWithoutEchoingTheValue() throws Exception {
        mockMvc.perform(get(BASE).param("status", "NOPE<script>"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("status"))
                .andExpect(content().string(not(containsString("NOPE"))));
    }

    @Test
    void search_withMalformedDate_isRejected() throws Exception {
        mockMvc.perform(get(BASE).param("effectiveDateFrom", "01/02/2026"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("effectiveDateFrom"));
    }

    // ---- GET /api/v1/policies/{id}

    @Test
    void getById_returnsPolicy() throws Exception {
        when(getPolicyByIdUseCase.execute(POLICY_ID)).thenReturn(aPolicy());

        mockMvc.perform(get(BASE + "/{id}", POLICY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(POLICY_ID.toString()))
                .andExpect(jsonPath("$.currency").value("SGD"))
                .andExpect(jsonPath("$.lineOfBusiness").value("HEALTH"))
                .andExpect(jsonPath("$.createdAt").value("2026-02-20T09:15:00"));
    }

    @Test
    void getById_whenMissing_returns404ProblemDetail() throws Exception {
        UUID id = UUID.randomUUID();
        when(getPolicyByIdUseCase.execute(id)).thenThrow(new PolicyNotFoundException(id));

        mockMvc.perform(get(BASE + "/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Policy Not Found"))
                .andExpect(jsonPath("$.detail").value("Policy with id " + id + " was not found"))
                .andExpect(jsonPath("$.instance").value(BASE + "/" + id))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void getById_withMalformedId_returns400() throws Exception {
        mockMvc.perform(get(BASE + "/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.requestId").exists());
    }

    // ---- PATCH /api/v1/policies/flag

    @Test
    void flag_returnsUpdatedPolicyAndPassesCommand() throws Exception {
        var flagged = aPolicy();
        flagged.flagForReview(true, flagged.getCreatedAt().plusDays(1));
        when(flagPolicyUseCase.execute(any())).thenReturn(flagged);

        mockMvc.perform(patch(BASE + "/flag").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"policyId\":\"" + POLICY_ID + "\",\"flaggedForReview\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flaggedForReview").value(true));

        ArgumentCaptor<FlagPolicyCommand> captor = ArgumentCaptor.forClass(FlagPolicyCommand.class);
        verify(flagPolicyUseCase).execute(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new FlagPolicyCommand(POLICY_ID, true));
    }

    @Test
    void flag_withMissingFields_returns400ListingThem() throws Exception {
        mockMvc.perform(patch(BASE + "/flag").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field=='policyId')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field=='flaggedForReview')]").exists());
    }

    @Test
    void flag_withMalformedJson_returns400() throws Exception {
        mockMvc.perform(patch(BASE + "/flag").contentType(MediaType.APPLICATION_JSON).content("{\"policyId\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void flag_withInvalidUuid_returns400() throws Exception {
        mockMvc.perform(patch(BASE + "/flag").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"policyId\":\"x\",\"flaggedForReview\":true}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void flag_whenPolicyMissing_returns404() throws Exception {
        when(flagPolicyUseCase.execute(any())).thenThrow(new PolicyNotFoundException(POLICY_ID));

        mockMvc.perform(patch(BASE + "/flag").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"policyId\":\"" + POLICY_ID + "\",\"flaggedForReview\":true}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Policy Not Found"));
    }

    @Test
    void flag_withUnsupportedMediaType_returns415() throws Exception {
        mockMvc.perform(patch(BASE + "/flag").contentType(MediaType.TEXT_PLAIN).content("x"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.requestId").exists());
    }

    // ---- GET /api/v1/policies/summary

    @Test
    void summary_returnsAggregates() throws Exception {
        when(getPolicySummaryUseCase.execute()).thenReturn(new PolicySummaryResult(
                260,
                Map.of(PolicyStatus.ACTIVE, 150L),
                List.of(new PremiumByLineOfBusiness(LineOfBusiness.HEALTH, "SGD", new BigDecimal("125430.50"))),
                18, 30));

        mockMvc.perform(get(BASE + "/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPolicies").value(260))
                .andExpect(jsonPath("$.countByStatus.ACTIVE").value(150))
                .andExpect(jsonPath("$.premiumByLineOfBusiness[0].lineOfBusiness").value("HEALTH"))
                .andExpect(jsonPath("$.premiumByLineOfBusiness[0].currency").value("SGD"))
                .andExpect(jsonPath("$.premiumByLineOfBusiness[0].totalPremium").value(125430.50))
                .andExpect(jsonPath("$.expiringSoonCount").value(18))
                .andExpect(jsonPath("$.expiringSoonDays").value(30));
    }

    @Test
    void summary_isRoutedToSummaryHandlerNotTreatedAsPolicyId() throws Exception {
        when(getPolicySummaryUseCase.execute())
                .thenReturn(new PolicySummaryResult(0, Map.of(), List.of(), 0, 30));

        mockMvc.perform(get(BASE + "/summary")).andExpect(status().isOk());
    }

    // ---- cross-cutting

    @Test
    void unexpectedFailure_returns500WithoutLeakingInternals() throws Exception {
        when(getPolicySummaryUseCase.execute()).thenThrow(new IllegalStateException("db password is hunter2"));

        mockMvc.perform(get(BASE + "/summary"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred"))
                .andExpect(content().string(not(containsString("hunter2"))));
    }

    @Test
    void requestIdFromClient_isEchoedInHeaderAndProblemBody() throws Exception {
        mockMvc.perform(get(BASE + "/not-a-uuid").header("X-Request-Id", "client-req-42"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("X-Request-Id", "client-req-42"))
                .andExpect(jsonPath("$.requestId").value("client-req-42"));
    }

    @Test
    void unsupportedMethod_returns405ProblemDetail() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(BASE))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.requestId").exists());
    }
}
