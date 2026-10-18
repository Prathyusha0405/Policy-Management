package com.hcl.chubb.policy.api.mapper;

import com.hcl.chubb.policy.api.dto.request.FlagPolicyRequest;
import com.hcl.chubb.policy.api.dto.request.PolicySearchRequest;
import com.hcl.chubb.policy.api.dto.response.PolicyPageResponse;
import com.hcl.chubb.policy.api.dto.response.PolicyResponse;
import com.hcl.chubb.policy.api.dto.response.PolicySummaryResponse;
import com.hcl.chubb.policy.api.dto.response.PolicySummaryResponse.PremiumByLineOfBusinessResponse;
import com.hcl.chubb.policy.application.command.FlagPolicyCommand;
import com.hcl.chubb.policy.application.query.PageQuery;
import com.hcl.chubb.policy.application.query.PolicySearchQuery;
import com.hcl.chubb.policy.application.query.PolicySortField;
import com.hcl.chubb.policy.application.query.SortDirection;
import com.hcl.chubb.policy.application.result.PageResult;
import com.hcl.chubb.policy.application.result.PolicySummaryResult;
import com.hcl.chubb.policy.domain.model.Policy;
import org.springframework.stereotype.Component;

@Component
public class PolicyApiMapper {

    public PolicySearchQuery toQuery(PolicySearchRequest request) {
        String[] sortParts = request.sort().split(",");
        PolicySortField sortField = PolicySortField.fromProperty(sortParts[0]);
        SortDirection direction = sortParts.length > 1 && "desc".equals(sortParts[1])
                ? SortDirection.DESC
                : SortDirection.ASC;

        return new PolicySearchQuery(
                request.search(),
                request.status(),
                request.lineOfBusiness(),
                request.region(),
                request.effectiveDateFrom(),
                request.effectiveDateTo(),
                new PageQuery(request.page(), request.size(), sortField, direction));
    }

    public FlagPolicyCommand toCommand(FlagPolicyRequest request) {
        return new FlagPolicyCommand(request.policyId(), request.flaggedForReview());
    }

    public PolicyResponse toResponse(Policy policy) {
        return new PolicyResponse(
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

    public PolicyPageResponse toPageResponse(PageResult<Policy> result, String sort) {
        return new PolicyPageResponse(
                result.content().stream().map(this::toResponse).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                sort);
    }

    public PolicySummaryResponse toResponse(PolicySummaryResult result) {
        return new PolicySummaryResponse(
                result.totalPolicies(),
                result.countByStatus(),
                result.premiumByLineOfBusiness().stream()
                        .map(p -> new PremiumByLineOfBusinessResponse(
                                p.lineOfBusiness(), p.currency(), p.totalPremium()))
                        .toList(),
                result.expiringSoonCount(),
                result.expiringSoonDays());
    }
}
