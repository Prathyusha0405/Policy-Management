package com.hcl.chubb.policy.infrastructure.persistence;

import com.hcl.chubb.policy.domain.model.Policy;
import com.hcl.chubb.policy.infrastructure.persistence.entity.PolicyEntity;
import com.hcl.chubb.policy.infrastructure.persistence.mapper.PolicyEntityMapper;
import org.junit.jupiter.api.Test;

import static com.hcl.chubb.policy.support.PolicyTestDataFactory.aPolicy;
import static org.assertj.core.api.Assertions.assertThat;

class PolicyEntityMapperTest {

    private final PolicyEntityMapper mapper = new PolicyEntityMapper();

    @Test
    void domainSurvivesRoundTripThroughEntity() {
        Policy policy = aPolicy();

        PolicyEntity entity = mapper.toEntity(policy);
        Policy restored = mapper.toDomain(entity);

        assertThat(entity.getId()).isEqualTo(policy.getId());
        assertThat(entity.isFlaggedForReview()).isFalse();
        assertThat(restored).usingRecursiveComparison().isEqualTo(policy);
    }

    @Test
    void flaggedStateIsPreserved() {
        Policy policy = aPolicy();
        policy.flagForReview(true, policy.getCreatedAt().plusDays(1));

        Policy restored = mapper.toDomain(mapper.toEntity(policy));

        assertThat(restored.isFlaggedForReview()).isTrue();
        assertThat(restored.getUpdatedAt()).isEqualTo(policy.getUpdatedAt());
    }
}
