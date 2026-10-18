package com.hcl.chubb.policy.infrastructure.persistence;

import com.hcl.chubb.policy.application.query.PageQuery;
import com.hcl.chubb.policy.application.query.PolicySearchQuery;
import com.hcl.chubb.policy.application.query.PolicySortField;
import com.hcl.chubb.policy.application.query.SortDirection;
import com.hcl.chubb.policy.application.result.PageResult;
import com.hcl.chubb.policy.application.result.PolicySummaryResult;
import com.hcl.chubb.policy.domain.model.LineOfBusiness;
import com.hcl.chubb.policy.domain.model.Policy;
import com.hcl.chubb.policy.domain.model.PolicyStatus;
import com.hcl.chubb.policy.infrastructure.persistence.adapter.PolicyPersistenceAdapter;
import com.hcl.chubb.policy.infrastructure.persistence.mapper.PolicyEntityMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs the real Flyway migrations (schema + seed data) on an embedded H2 database and exercises
 * the specifications, sorting, paging and aggregation queries through the persistence adapter.
 */
@DataJpaTest
@Import({PolicyPersistenceAdapter.class, PolicyEntityMapper.class})
class PolicyPersistenceAdapterIntegrationTest {

    private static final PageQuery FIRST_PAGE_OF_ALL = page(0, 500, PolicySortField.POLICY_NUMBER, SortDirection.ASC);

    @Autowired
    private PolicyPersistenceAdapter adapter;

    private static PageQuery page(int page, int size, PolicySortField field, SortDirection direction) {
        return new PageQuery(page, size, field, direction);
    }

    private static PolicySearchQuery query(String search, PolicyStatus status, LineOfBusiness lob, String region,
                                           LocalDate from, LocalDate to, PageQuery page) {
        return new PolicySearchQuery(search, status, lob, region, from, to, page);
    }

    private static PolicySearchQuery unfiltered(PageQuery page) {
        return query(null, null, null, null, null, null, page);
    }

    @Test
    void seedData_hasAtLeast250PoliciesAcrossStatusesCurrenciesAndRegions() {
        PageResult<Policy> all = adapter.search(unfiltered(FIRST_PAGE_OF_ALL));

        assertThat(all.totalElements()).isGreaterThanOrEqualTo(250);
        assertThat(all.content()).extracting(Policy::getStatus).containsAll(List.of(PolicyStatus.values()));
        assertThat(all.content().stream().map(Policy::getCurrency).distinct().count()).isGreaterThan(5);
        assertThat(all.content().stream().map(Policy::getRegion).distinct().count()).isGreaterThan(5);
    }

    @Test
    void search_paginatesWithStableNonOverlappingPages() {
        PageQuery first = page(0, 10, PolicySortField.STATUS, SortDirection.ASC);
        PageQuery second = page(1, 10, PolicySortField.STATUS, SortDirection.ASC);

        PageResult<Policy> p1 = adapter.search(unfiltered(first));
        PageResult<Policy> p2 = adapter.search(unfiltered(second));

        assertThat(p1.content()).hasSize(10);
        assertThat(p2.content()).hasSize(10);
        assertThat(p1.page()).isZero();
        assertThat(p2.page()).isEqualTo(1);
        assertThat(p1.totalPages()).isEqualTo((int) Math.ceil(p1.totalElements() / 10.0));
        assertThat(p1.content()).extracting(Policy::getId)
                .doesNotContainAnyElementsOf(p2.content().stream().map(Policy::getId).toList());
    }

    @Test
    void search_pageBeyondTheEnd_isEmptyButReportsTotals() {
        PageResult<Policy> result = adapter.search(unfiltered(page(1000, 10, PolicySortField.POLICY_NUMBER, SortDirection.ASC)));

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isGreaterThanOrEqualTo(250);
    }

    @Test
    void search_sortsByPremiumInBothDirections() {
        List<Policy> desc = adapter.search(unfiltered(
                page(0, 50, PolicySortField.PREMIUM_AMOUNT, SortDirection.DESC))).content();
        List<Policy> asc = adapter.search(unfiltered(
                page(0, 50, PolicySortField.PREMIUM_AMOUNT, SortDirection.ASC))).content();

        assertThat(desc).isSortedAccordingTo(Comparator.comparing(Policy::getPremiumAmount).reversed());
        assertThat(asc).isSortedAccordingTo(Comparator.comparing(Policy::getPremiumAmount));
    }

    @Test
    void search_filtersByStatus() {
        PageResult<Policy> result = adapter.search(query(null, PolicyStatus.CANCELLED, null, null, null, null,
                FIRST_PAGE_OF_ALL));

        assertThat(result.content()).isNotEmpty()
                .allMatch(p -> p.getStatus() == PolicyStatus.CANCELLED);
    }

    @Test
    void search_filtersByLineOfBusiness() {
        PageResult<Policy> result = adapter.search(query(null, null, LineOfBusiness.MARINE, null, null, null,
                FIRST_PAGE_OF_ALL));

        assertThat(result.content()).isNotEmpty()
                .allMatch(p -> p.getLineOfBusiness() == LineOfBusiness.MARINE);
    }

    @Test
    void search_filtersByRegionIgnoringCase() {
        PageResult<Policy> exact = adapter.search(query(null, null, null, "Singapore", null, null, FIRST_PAGE_OF_ALL));
        PageResult<Policy> shouting = adapter.search(query(null, null, null, "SINGAPORE", null, null, FIRST_PAGE_OF_ALL));

        assertThat(exact.content()).isNotEmpty().allMatch(p -> p.getRegion().equals("Singapore"));
        assertThat(shouting.totalElements()).isEqualTo(exact.totalElements());
    }

    @Test
    void search_filtersByEffectiveDateRangeInclusively() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 3, 31);

        PageResult<Policy> result = adapter.search(query(null, null, null, null, from, to, FIRST_PAGE_OF_ALL));

        assertThat(result.content()).isNotEmpty()
                .allMatch(p -> !p.getEffectiveDate().isBefore(from) && !p.getEffectiveDate().isAfter(to));
    }

    @Test
    void search_supportsOpenEndedDateRanges() {
        LocalDate boundary = LocalDate.of(2026, 6, 1);

        PageResult<Policy> from = adapter.search(query(null, null, null, null, boundary, null, FIRST_PAGE_OF_ALL));
        PageResult<Policy> to = adapter.search(query(null, null, null, null, null, boundary, FIRST_PAGE_OF_ALL));

        assertThat(from.content()).isNotEmpty().allMatch(p -> !p.getEffectiveDate().isBefore(boundary));
        assertThat(to.content()).isNotEmpty().allMatch(p -> !p.getEffectiveDate().isAfter(boundary));
    }

    @Test
    void search_freeTextMatchesPolicyNumberCaseInsensitively() {
        PageResult<Policy> result = adapter.search(query("pol-sg-", null, null, null, null, null, FIRST_PAGE_OF_ALL));

        assertThat(result.content()).isNotEmpty().allMatch(p -> p.getPolicyNumber().startsWith("POL-SG-"));
    }

    @Test
    void search_freeTextMatchesPolicyholderNameAndUnderwriter() {
        PageResult<Policy> byName = adapter.search(query("Tan", null, null, null, null, null, FIRST_PAGE_OF_ALL));
        PageResult<Policy> byUnderwriter = adapter.search(query("Priya Nair", null, null, null, null, null, FIRST_PAGE_OF_ALL));

        assertThat(byName.content()).isNotEmpty()
                .allMatch(p -> p.getPolicyholderName().toLowerCase().contains("tan")
                        || p.getPolicyNumber().toLowerCase().contains("tan")
                        || p.getUnderwriter().toLowerCase().contains("tan"));
        assertThat(byUnderwriter.content()).isNotEmpty()
                .allMatch(p -> p.getUnderwriter().equals("Priya Nair"));
    }

    @Test
    void search_treatsLikeWildcardsAsLiteralText() {
        assertThat(adapter.search(query("%", null, null, null, null, null, FIRST_PAGE_OF_ALL)).totalElements()).isZero();
        assertThat(adapter.search(query("_", null, null, null, null, null, FIRST_PAGE_OF_ALL)).totalElements()).isZero();
    }

    @Test
    void search_combinesFiltersWithAnd() {
        PageResult<Policy> result = adapter.search(query("pol-sg-", PolicyStatus.ACTIVE, LineOfBusiness.HEALTH,
                "Singapore", null, null, FIRST_PAGE_OF_ALL));

        assertThat(result.content()).allMatch(p -> p.getPolicyNumber().startsWith("POL-SG-")
                && p.getStatus() == PolicyStatus.ACTIVE
                && p.getLineOfBusiness() == LineOfBusiness.HEALTH
                && p.getRegion().equals("Singapore"));
    }

    @Test
    void search_withNoMatches_returnsEmptyPage() {
        PageResult<Policy> result = adapter.search(query("no-such-policyholder-xyz", null, null, null, null, null,
                FIRST_PAGE_OF_ALL));

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
        assertThat(result.totalPages()).isZero();
    }

    @Test
    void findById_returnsMappedPolicyOrEmpty() {
        Policy any = adapter.search(unfiltered(page(0, 1, PolicySortField.POLICY_NUMBER, SortDirection.ASC)))
                .content().get(0);

        assertThat(adapter.findById(any.getId())).get().usingRecursiveComparison().isEqualTo(any);
        assertThat(adapter.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void save_persistsFlagChange() {
        Policy policy = adapter.search(query(null, PolicyStatus.ACTIVE, null, null, null, null,
                page(0, 1, PolicySortField.POLICY_NUMBER, SortDirection.ASC))).content().get(0);
        boolean target = !policy.isFlaggedForReview();
        LocalDateTime now = LocalDateTime.of(2026, 10, 18, 10, 0);

        policy.flagForReview(target, now);
        adapter.save(policy);

        Policy reloaded = adapter.findById(policy.getId()).orElseThrow();
        assertThat(reloaded.isFlaggedForReview()).isEqualTo(target);
        assertThat(reloaded.getUpdatedAt()).isEqualTo(now);
        assertThat(reloaded.getCreatedAt()).isEqualTo(policy.getCreatedAt());
    }

    @Test
    void summarize_countsEveryPolicyByStatusIncludingEmptyStatuses() {
        PolicySummaryResult summary = adapter.summarize(LocalDate.of(2026, 10, 18), 30);
        long total = adapter.search(unfiltered(page(0, 1, PolicySortField.POLICY_NUMBER, SortDirection.ASC)))
                .totalElements();

        assertThat(summary.totalPolicies()).isEqualTo(total);
        assertThat(summary.countByStatus()).containsKeys(PolicyStatus.values());
        assertThat(summary.countByStatus().values().stream().mapToLong(Long::longValue).sum()).isEqualTo(total);
        for (PolicyStatus status : PolicyStatus.values()) {
            long expected = adapter.search(query(null, status, null, null, null, null,
                    page(0, 1, PolicySortField.POLICY_NUMBER, SortDirection.ASC))).totalElements();
            assertThat(summary.countByStatus().get(status)).isEqualTo(expected);
        }
    }

    @Test
    void summarize_groupsPremiumByLineOfBusinessAndCurrencyWithoutMixingCurrencies() {
        PolicySummaryResult summary = adapter.summarize(LocalDate.of(2026, 10, 18), 30);
        List<Policy> all = adapter.search(unfiltered(FIRST_PAGE_OF_ALL)).content();

        assertThat(summary.premiumByLineOfBusiness()).isNotEmpty()
                .extracting(p -> p.lineOfBusiness() + "/" + p.currency())
                .doesNotHaveDuplicates();
        summary.premiumByLineOfBusiness().forEach(group -> {
            var expected = all.stream()
                    .filter(p -> p.getLineOfBusiness() == group.lineOfBusiness()
                            && p.getCurrency().equals(group.currency()))
                    .map(Policy::getPremiumAmount)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            assertThat(group.totalPremium()).isEqualByComparingTo(expected);
        });
    }

    @Test
    void summarize_countsOnlyActivePoliciesExpiringWithinTheWindow() {
        LocalDate today = LocalDate.of(2026, 10, 18);
        List<Policy> active = adapter.search(query(null, PolicyStatus.ACTIVE, null, null, null, null,
                FIRST_PAGE_OF_ALL)).content();

        long expected30 = active.stream()
                .filter(p -> !p.getExpiryDate().isBefore(today) && !p.getExpiryDate().isAfter(today.plusDays(30)))
                .count();
        long expected0 = active.stream().filter(p -> p.getExpiryDate().isEqual(today)).count();

        PolicySummaryResult thirtyDays = adapter.summarize(today, 30);
        PolicySummaryResult zeroDays = adapter.summarize(today, 0);

        assertThat(thirtyDays.expiringSoonCount()).isEqualTo(expected30);
        assertThat(thirtyDays.expiringSoonDays()).isEqualTo(30);
        assertThat(zeroDays.expiringSoonCount()).isEqualTo(expected0);
    }
}
