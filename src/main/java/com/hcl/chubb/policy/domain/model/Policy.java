package com.hcl.chubb.policy.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Policy {

    private final UUID id;
    private final String policyNumber;
    private final String policyholderName;
    private final LineOfBusiness lineOfBusiness;
    private final PolicyStatus status;
    private final BigDecimal premiumAmount;
    private final String currency;
    private final LocalDate effectiveDate;
    private final LocalDate expiryDate;
    private final String region;
    private final String underwriter;
    private final LocalDateTime createdAt;

    private boolean flaggedForReview;
    private LocalDateTime updatedAt;

    public Policy(UUID id, String policyNumber, String policyholderName, LineOfBusiness lineOfBusiness,
                  PolicyStatus status, BigDecimal premiumAmount, String currency, LocalDate effectiveDate,
                  LocalDate expiryDate, String region, String underwriter, boolean flaggedForReview,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.policyNumber = Objects.requireNonNull(policyNumber, "policyNumber");
        this.policyholderName = Objects.requireNonNull(policyholderName, "policyholderName");
        this.lineOfBusiness = Objects.requireNonNull(lineOfBusiness, "lineOfBusiness");
        this.status = Objects.requireNonNull(status, "status");
        this.premiumAmount = Objects.requireNonNull(premiumAmount, "premiumAmount");
        this.currency = Objects.requireNonNull(currency, "currency");
        this.effectiveDate = Objects.requireNonNull(effectiveDate, "effectiveDate");
        this.expiryDate = Objects.requireNonNull(expiryDate, "expiryDate");
        this.region = Objects.requireNonNull(region, "region");
        this.underwriter = Objects.requireNonNull(underwriter, "underwriter");
        this.flaggedForReview = flaggedForReview;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    /**
     * Sets the review flag. The modification timestamp only moves when the value actually changes.
     *
     * @return true if the flag changed
     */
    public boolean flagForReview(boolean flagged, LocalDateTime now) {
        if (this.flaggedForReview == flagged) {
            return false;
        }
        this.flaggedForReview = flagged;
        this.updatedAt = Objects.requireNonNull(now, "now");
        return true;
    }

    public UUID getId() {
        return id;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public String getPolicyholderName() {
        return policyholderName;
    }

    public LineOfBusiness getLineOfBusiness() {
        return lineOfBusiness;
    }

    public PolicyStatus getStatus() {
        return status;
    }

    public BigDecimal getPremiumAmount() {
        return premiumAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public String getRegion() {
        return region;
    }

    public String getUnderwriter() {
        return underwriter;
    }

    public boolean isFlaggedForReview() {
        return flaggedForReview;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
