package com.hcl.chubb.policy.infrastructure.persistence.mapper;

import com.hcl.chubb.policy.domain.model.Policy;
import com.hcl.chubb.policy.infrastructure.persistence.entity.PolicyEntity;
import org.springframework.stereotype.Component;

@Component
public class PolicyEntityMapper {

    public Policy toDomain(PolicyEntity entity) {
        return new Policy(
                entity.getId(),
                entity.getPolicyNumber(),
                entity.getPolicyholderName(),
                entity.getLineOfBusiness(),
                entity.getStatus(),
                entity.getPremiumAmount(),
                entity.getCurrency(),
                entity.getEffectiveDate(),
                entity.getExpiryDate(),
                entity.getRegion(),
                entity.getUnderwriter(),
                entity.isFlaggedForReview(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public PolicyEntity toEntity(Policy policy) {
        return new PolicyEntity(
                policy.getId(),
                policy.getPolicyNumber(),
                policy.getPolicyholderName(),
                policy.getLineOfBusiness(),
                policy.getStatus(),
                policy.getPremiumAmount(),
                policy.getCurrency(),
                policy.getEffectiveDate(),
                policy.getExpiryDate(),
                policy.getRegion(),
                policy.getUnderwriter(),
                policy.isFlaggedForReview(),
                policy.getCreatedAt(),
                policy.getUpdatedAt());
    }
}
