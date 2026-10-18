package com.hcl.chubb.policy.api.dto;

import com.hcl.chubb.policy.api.dto.request.PolicySearchRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PolicySearchRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeFactory() {
        factory.close();
    }

    private static PolicySearchRequest of(Integer page, Integer size, String sort, String search, String region,
                                          LocalDate from, LocalDate to) {
        return new PolicySearchRequest(page, size, sort, search, null, null, region, from, to);
    }

    private static Set<String> violatedProperties(PolicySearchRequest request) {
        return validator.validate(request).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(java.util.stream.Collectors.toSet());
    }

    @Test
    void emptyRequest_isValidAndGetsDefaults() {
        PolicySearchRequest request = of(null, null, null, null, null, null, null);

        assertThat(validator.validate(request)).isEmpty();
        assertThat(request.page()).isZero();
        assertThat(request.size()).isEqualTo(20);
        assertThat(request.sort()).isEqualTo(PolicySearchRequest.DEFAULT_SORT);
    }

    @Test
    void negativePage_isRejected() {
        assertThat(violatedProperties(of(-1, 10, null, null, null, null, null))).containsExactly("page");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 101})
    void sizeOutsideOneToHundred_isRejected(int size) {
        assertThat(violatedProperties(of(0, size, null, null, null, null, null))).containsExactly("size");
    }

    @ParameterizedTest
    @ValueSource(strings = {"bogus", "premiumAmount,sideways", "premiumAmount,desc,extra", "policyNumber;drop"})
    void unsupportedSort_isRejected(String sort) {
        assertThat(violatedProperties(of(0, 10, sort, null, null, null, null))).containsExactly("sort");
    }

    @ParameterizedTest
    @ValueSource(strings = {"policyNumber", "premiumAmount,desc", "updatedAt,asc"})
    void supportedSort_isAccepted(String sort) {
        assertThat(validator.validate(of(0, 10, sort, null, null, null, null))).isEmpty();
    }

    @Test
    void searchAndRegionLengthLimits_areEnforced() {
        assertThat(violatedProperties(of(0, 10, null, "x".repeat(101), "y".repeat(51), null, null)))
                .containsExactlyInAnyOrder("search", "region");
        assertThat(violatedProperties(of(0, 10, null, "", "", null, null)))
                .containsExactlyInAnyOrder("search", "region");
    }

    @Test
    void reversedDateRange_isRejected() {
        PolicySearchRequest request = of(0, 10, null, null, null,
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 1, 1));

        assertThat(violatedProperties(request)).containsExactly("dateRangeValid");
    }

    @Test
    void sameDayAndOpenEndedRanges_areAccepted() {
        LocalDate day = LocalDate.of(2026, 5, 1);

        assertThat(validator.validate(of(0, 10, null, null, null, day, day))).isEmpty();
        assertThat(validator.validate(of(0, 10, null, null, null, day, null))).isEmpty();
        assertThat(validator.validate(of(0, 10, null, null, null, null, day))).isEmpty();
    }
}
