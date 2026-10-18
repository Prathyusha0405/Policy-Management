package com.hcl.chubb.policy.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;

@Validated
@ConfigurationProperties(prefix = "policy.summary")
public record PolicyProperties(@DefaultValue("30") @Min(1) int expiringSoonDays) {
}
