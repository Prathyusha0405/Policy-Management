package com.hcl.chubb.policy.application.result;

import com.hcl.chubb.policy.domain.model.LineOfBusiness;

import java.math.BigDecimal;

public record PremiumByLineOfBusiness(LineOfBusiness lineOfBusiness, String currency, BigDecimal totalPremium) {
}
