package com.hcl.chubb.policy.infrastructure.persistence.entity;

import com.hcl.chubb.policy.domain.model.LineOfBusiness;
import com.hcl.chubb.policy.domain.model.PolicyStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "policy")
public class PolicyEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "policy_number", nullable = false, length = 30)
    private String policyNumber;

    @Column(name = "policyholder_name", nullable = false, length = 150)
    private String policyholderName;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "line_of_business", nullable = false, length = 20)
    private LineOfBusiness lineOfBusiness;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false, length = 20)
    private PolicyStatus status;

    @Column(name = "premium_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal premiumAmount;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "region", nullable = false, length = 50)
    private String region;

    @Column(name = "underwriter", nullable = false, length = 100)
    private String underwriter;

    @Column(name = "flagged_for_review", nullable = false)
    private boolean flaggedForReview;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected PolicyEntity() {
    }

    public PolicyEntity(UUID id, String policyNumber, String policyholderName, LineOfBusiness lineOfBusiness,
                        PolicyStatus status, BigDecimal premiumAmount, String currency, LocalDate effectiveDate,
                        LocalDate expiryDate, String region, String underwriter, boolean flaggedForReview,
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.policyNumber = policyNumber;
        this.policyholderName = policyholderName;
        this.lineOfBusiness = lineOfBusiness;
        this.status = status;
        this.premiumAmount = premiumAmount;
        this.currency = currency;
        this.effectiveDate = effectiveDate;
        this.expiryDate = expiryDate;
        this.region = region;
        this.underwriter = underwriter;
        this.flaggedForReview = flaggedForReview;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
