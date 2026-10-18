package com.hcl.chubb.policy.api.controller;

import com.hcl.chubb.policy.api.dto.request.FlagPolicyRequest;
import com.hcl.chubb.policy.api.dto.request.PolicySearchRequest;
import com.hcl.chubb.policy.api.dto.response.PolicyPageResponse;
import com.hcl.chubb.policy.api.dto.response.PolicyResponse;
import com.hcl.chubb.policy.api.dto.response.PolicySummaryResponse;
import com.hcl.chubb.policy.api.mapper.PolicyApiMapper;
import com.hcl.chubb.policy.application.usecase.FlagPolicyUseCase;
import com.hcl.chubb.policy.application.usecase.GetPolicyByIdUseCase;
import com.hcl.chubb.policy.application.usecase.GetPolicySummaryUseCase;
import com.hcl.chubb.policy.application.usecase.SearchPoliciesUseCase;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/policies")
public class PolicyController {

    private final SearchPoliciesUseCase searchPoliciesUseCase;
    private final GetPolicyByIdUseCase getPolicyByIdUseCase;
    private final FlagPolicyUseCase flagPolicyUseCase;
    private final GetPolicySummaryUseCase getPolicySummaryUseCase;
    private final PolicyApiMapper mapper;

    public PolicyController(SearchPoliciesUseCase searchPoliciesUseCase, GetPolicyByIdUseCase getPolicyByIdUseCase,
                            FlagPolicyUseCase flagPolicyUseCase, GetPolicySummaryUseCase getPolicySummaryUseCase,
                            PolicyApiMapper mapper) {
        this.searchPoliciesUseCase = searchPoliciesUseCase;
        this.getPolicyByIdUseCase = getPolicyByIdUseCase;
        this.flagPolicyUseCase = flagPolicyUseCase;
        this.getPolicySummaryUseCase = getPolicySummaryUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    public PolicyPageResponse searchPolicies(@ParameterObject @Valid PolicySearchRequest request) {
        return mapper.toPageResponse(searchPoliciesUseCase.execute(mapper.toQuery(request)), request.sort());
    }

    @GetMapping("/{id}")
    public PolicyResponse getPolicyById(@PathVariable UUID id) {
        return mapper.toResponse(getPolicyByIdUseCase.execute(id));
    }

    @PatchMapping("/flag")
    public PolicyResponse flagPolicy(@Valid @RequestBody FlagPolicyRequest request) {
        return mapper.toResponse(flagPolicyUseCase.execute(mapper.toCommand(request)));
    }

    @GetMapping("/summary")
    public PolicySummaryResponse getPolicySummary() {
        return mapper.toResponse(getPolicySummaryUseCase.execute());
    }
}
