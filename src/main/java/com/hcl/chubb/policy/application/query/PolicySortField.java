package com.hcl.chubb.policy.application.query;

import java.util.Arrays;

/**
 * Whitelist of sortable fields. The property name is the public API name and
 * the persistence attribute name.
 */
public enum PolicySortField {
    POLICY_NUMBER("policyNumber"),
    POLICYHOLDER_NAME("policyholderName"),
    LINE_OF_BUSINESS("lineOfBusiness"),
    STATUS("status"),
    PREMIUM_AMOUNT("premiumAmount"),
    EFFECTIVE_DATE("effectiveDate"),
    EXPIRY_DATE("expiryDate"),
    REGION("region"),
    UNDERWRITER("underwriter"),
    CREATED_AT("createdAt"),
    UPDATED_AT("updatedAt");

    private final String property;

    PolicySortField(String property) {
        this.property = property;
    }

    public String property() {
        return property;
    }

    public static PolicySortField fromProperty(String property) {
        return Arrays.stream(values())
                .filter(field -> field.property.equals(property))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported sort field: " + property));
    }
}
