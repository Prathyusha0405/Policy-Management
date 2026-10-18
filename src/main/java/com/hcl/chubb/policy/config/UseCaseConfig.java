package com.hcl.chubb.policy.config;

import com.hcl.chubb.policy.application.port.out.PolicyCommandPort;
import com.hcl.chubb.policy.application.port.out.PolicyQueryPort;
import com.hcl.chubb.policy.application.port.out.PolicySummaryPort;
import com.hcl.chubb.policy.application.port.out.SummaryCachePort;
import com.hcl.chubb.policy.application.usecase.FlagPolicyUseCase;
import com.hcl.chubb.policy.application.usecase.GetPolicyByIdUseCase;
import com.hcl.chubb.policy.application.usecase.GetPolicySummaryUseCase;
import com.hcl.chubb.policy.application.usecase.SearchPoliciesUseCase;
import com.hcl.chubb.policy.config.properties.PolicyProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Wires the framework-free use cases so the application layer carries no Spring annotations.
 */
@Configuration
@EnableConfigurationProperties(PolicyProperties.class)
public class UseCaseConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public SearchPoliciesUseCase searchPoliciesUseCase(PolicyQueryPort policyQueryPort) {
        return new SearchPoliciesUseCase(policyQueryPort);
    }

    @Bean
    public GetPolicyByIdUseCase getPolicyByIdUseCase(PolicyQueryPort policyQueryPort) {
        return new GetPolicyByIdUseCase(policyQueryPort);
    }

    @Bean
    public FlagPolicyUseCase flagPolicyUseCase(PolicyQueryPort policyQueryPort, PolicyCommandPort policyCommandPort,
                                               SummaryCachePort summaryCachePort, Clock clock) {
        return new FlagPolicyUseCase(policyQueryPort, policyCommandPort, summaryCachePort, clock);
    }

    @Bean
    public GetPolicySummaryUseCase getPolicySummaryUseCase(PolicySummaryPort policySummaryPort, Clock clock,
                                                           PolicyProperties properties) {
        return new GetPolicySummaryUseCase(policySummaryPort, clock, properties.expiringSoonDays());
    }
}
