package com.hcl.chubb.policy.api.dto.request;

import com.hcl.chubb.policy.domain.model.LineOfBusiness;
import com.hcl.chubb.policy.domain.model.PolicyStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record PolicySearchRequest(
        @Min(0) Integer page,
        @Min(1) @Max(100) Integer size,
        @Schema(allowableValues = {
                "policyNumber,asc", "policyNumber,desc", "policyholderName,asc", "policyholderName,desc",
                "lineOfBusiness,asc", "lineOfBusiness,desc", "status,asc", "status,desc",
                "premiumAmount,asc", "premiumAmount,desc", "effectiveDate,asc", "effectiveDate,desc",
                "expiryDate,asc", "expiryDate,desc", "region,asc", "region,desc",
                "underwriter,asc", "underwriter,desc", "createdAt,asc", "createdAt,desc",
                "updatedAt,asc", "updatedAt,desc"}, defaultValue = DEFAULT_SORT)
        @Pattern(regexp = SORT_PATTERN, message = "must be field[,asc|desc] using a supported field") String sort,
        @Size(min = 1, max = 100) String search,
        PolicyStatus status,
        LineOfBusiness lineOfBusiness,
        @Schema(description = "APAC region (exact match, case-insensitive).", allowableValues = {
                "Australia", "China", "Hong Kong", "India", "Indonesia", "Japan", "Malaysia", "New Zealand",
                "Philippines", "Singapore", "South Korea", "Taiwan", "Thailand", "Vietnam"})
        @Size(min = 1, max = 50) String region,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveDateFrom,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveDateTo) {

    public static final String DEFAULT_SORT = "effectiveDate,desc";

    private static final String SORT_PATTERN =
            "^(policyNumber|policyholderName|lineOfBusiness|status|premiumAmount|effectiveDate|expiryDate"
                    + "|region|underwriter|createdAt|updatedAt)(,(asc|desc))?$";

    public PolicySearchRequest {
        page = page == null ? 0 : page;
        size = size == null ? 20 : size;
        sort = sort == null ? DEFAULT_SORT : sort;
    }

    @AssertTrue(message = "effectiveDateFrom must be on or before effectiveDateTo")
    public boolean isDateRangeValid() {
        return effectiveDateFrom == null || effectiveDateTo == null || !effectiveDateFrom.isAfter(effectiveDateTo);
    }
}
