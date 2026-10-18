package com.hcl.chubb.policy.integration;

import com.hcl.chubb.policy.infrastructure.cache.CacheNames;
import com.hcl.chubb.policy.infrastructure.persistence.entity.PolicyEntity;
import com.hcl.chubb.policy.infrastructure.persistence.repository.PolicyJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack tests: HTTP -> controller -> use case -> JPA -> H2 with Flyway-seeded data.
 * Tests that mutate data are {@code @Transactional}, so MockMvc requests (same thread) roll back.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PolicyApiIntegrationTest {

    private static final String BASE = "/api/v1/policies";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PolicyJpaRepository repository;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private Clock clock;

    @BeforeEach
    void clearSummaryCache() {
        cacheManager.getCache(CacheNames.POLICY_SUMMARY).clear();
    }

    @Test
    void search_defaultsToFirstTwentyPoliciesNewestEffectiveDateFirst() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(20))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(250)))
                .andExpect(jsonPath("$.sort").value("effectiveDate,desc"));
    }

    @Test
    void search_appliesFiltersAndFreeTextTogether() throws Exception {
        mockMvc.perform(get(BASE)
                        .param("search", "pol-sg-").param("status", "ACTIVE").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].status", everyItem(is("ACTIVE"))))
                .andExpect(jsonPath("$.content[*].policyNumber", everyItem(startsWith("POL-SG-"))));
    }

    @Test
    void search_returnsSortedResults() throws Exception {
        MvcResult result = mockMvc.perform(get(BASE).param("sort", "premiumAmount,desc").param("size", "30"))
                .andExpect(status().isOk())
                .andReturn();

        // first element must be the largest premium among those returned
        String body = result.getResponse().getContentAsString();
        java.util.List<Double> premiums = com.jayway.jsonpath.JsonPath.read(body, "$.content[*].premiumAmount");
        assertThat(premiums).isSortedAccordingTo(java.util.Comparator.reverseOrder());
    }

    @Test
    void search_rejectsInvalidInputWithProblemDetails() throws Exception {
        mockMvc.perform(get(BASE).param("size", "1000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("size"))
                .andExpect(jsonPath("$.requestId").exists());
    }

    @Test
    void getById_returnsPersistedPolicy_and404ForUnknownId() throws Exception {
        PolicyEntity entity = repository.findAll().get(0);

        mockMvc.perform(get(BASE + "/{id}", entity.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(entity.getId().toString()))
                .andExpect(jsonPath("$.policyNumber").value(entity.getPolicyNumber()))
                .andExpect(jsonPath("$.currency").value(entity.getCurrency().trim()));

        mockMvc.perform(get(BASE + "/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Policy Not Found"));
    }

    @Test
    void flag_persistsChangeAndIsIdempotent() throws Exception {
        PolicyEntity entity = repository.findAll().get(0);
        boolean target = !entity.isFlaggedForReview();
        String body = "{\"policyId\":\"" + entity.getId() + "\",\"flaggedForReview\":" + target + "}";

        mockMvc.perform(patch(BASE + "/flag").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flaggedForReview").value(target));
        repository.flush();
        assertThat(repository.findById(entity.getId()).orElseThrow().isFlaggedForReview()).isEqualTo(target);

        // repeating the same request succeeds and changes nothing
        mockMvc.perform(patch(BASE + "/flag").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flaggedForReview").value(target));
    }

    @Test
    void flag_unknownPolicy_returns404() throws Exception {
        mockMvc.perform(patch(BASE + "/flag").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"policyId\":\"" + UUID.randomUUID() + "\",\"flaggedForReview\":true}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void summary_reflectsSeedDataAndIsCached() throws Exception {
        mockMvc.perform(get(BASE + "/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPolicies", greaterThanOrEqualTo(250)))
                .andExpect(jsonPath("$.countByStatus.ACTIVE").exists())
                .andExpect(jsonPath("$.countByStatus.EXPIRED").exists())
                .andExpect(jsonPath("$.premiumByLineOfBusiness").isNotEmpty())
                .andExpect(jsonPath("$.expiringSoonDays").value(30));

        assertThat(summaryCache().get(summaryKey())).isNotNull();
    }

    @Test
    void flagging_evictsTheCachedSummary() throws Exception {
        mockMvc.perform(get(BASE + "/summary")).andExpect(status().isOk());
        assertThat(summaryCache().get(summaryKey())).as("summary cached after first read").isNotNull();

        PolicyEntity entity = repository.findAll().get(0);
        String body = "{\"policyId\":\"" + entity.getId() + "\",\"flaggedForReview\":"
                + !entity.isFlaggedForReview() + "}";
        mockMvc.perform(patch(BASE + "/flag").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());

        assertThat(summaryCache().get(summaryKey())).as("summary evicted by flag").isNull();
    }

    @Test
    void flaggingWithoutChange_keepsTheCachedSummary() throws Exception {
        mockMvc.perform(get(BASE + "/summary")).andExpect(status().isOk());
        PolicyEntity entity = repository.findAll().get(0);
        String body = "{\"policyId\":\"" + entity.getId() + "\",\"flaggedForReview\":"
                + entity.isFlaggedForReview() + "}";

        mockMvc.perform(patch(BASE + "/flag").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());

        assertThat(summaryCache().get(summaryKey())).isNotNull();
    }

    private Cache summaryCache() {
        return cacheManager.getCache(CacheNames.POLICY_SUMMARY);
    }

    private String summaryKey() {
        return LocalDate.now(clock) + ":30";
    }
}
