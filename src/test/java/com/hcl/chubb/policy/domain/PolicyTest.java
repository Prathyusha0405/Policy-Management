package com.hcl.chubb.policy.domain;

import com.hcl.chubb.policy.domain.model.LineOfBusiness;
import com.hcl.chubb.policy.domain.model.Policy;
import com.hcl.chubb.policy.domain.model.PolicyStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.hcl.chubb.policy.support.PolicyTestDataFactory.CREATED_AT;
import static com.hcl.chubb.policy.support.PolicyTestDataFactory.aPolicy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class PolicyTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 18, 10, 0);

    @Test
    void flagForReview_whenValueChanges_setsFlagAndUpdatesTimestamp() {
        Policy policy = aPolicy();

        boolean changed = policy.flagForReview(true, NOW);

        assertThat(changed).isTrue();
        assertThat(policy.isFlaggedForReview()).isTrue();
        assertThat(policy.getUpdatedAt()).isEqualTo(NOW);
        assertThat(policy.getCreatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void flagForReview_canUnflag() {
        Policy policy = aPolicy(aPolicy().getId(), true);

        boolean changed = policy.flagForReview(false, NOW);

        assertThat(changed).isTrue();
        assertThat(policy.isFlaggedForReview()).isFalse();
        assertThat(policy.getUpdatedAt()).isEqualTo(NOW);
    }

    @Test
    void flagForReview_whenValueUnchanged_returnsFalseAndKeepsTimestamp() {
        Policy policy = aPolicy();

        boolean changed = policy.flagForReview(false, NOW);

        assertThat(changed).isFalse();
        assertThat(policy.isFlaggedForReview()).isFalse();
        assertThat(policy.getUpdatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void constructor_rejectsMissingRequiredField() {
        assertThatNullPointerException()
                .isThrownBy(() -> new Policy(UUID.randomUUID(), null, "Name", LineOfBusiness.MOTOR,
                        PolicyStatus.ACTIVE, BigDecimal.TEN, "SGD", LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 12, 31), "Singapore", "UW", false, CREATED_AT, CREATED_AT))
                .withMessageContaining("policyNumber");
    }
}
