package com.hcl.chubb.policy.infrastructure.persistence.specification;

import com.hcl.chubb.policy.application.query.PolicySearchQuery;
import com.hcl.chubb.policy.infrastructure.persistence.entity.PolicyEntity;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PolicySpecifications {

    private static final char ESCAPE = '\\';

    private PolicySpecifications() {
    }

    public static Specification<PolicyEntity> from(PolicySearchQuery query) {
        List<Specification<PolicyEntity>> specifications = new ArrayList<>();

        if (hasText(query.search())) {
            specifications.add(matchesFreeText(query.search().trim()));
        }
        if (query.status() != null) {
            specifications.add((root, cq, cb) -> cb.equal(root.get("status"), query.status()));
        }
        if (query.lineOfBusiness() != null) {
            specifications.add((root, cq, cb) -> cb.equal(root.get("lineOfBusiness"), query.lineOfBusiness()));
        }
        if (hasText(query.region())) {
            String region = query.region().trim().toLowerCase(Locale.ROOT);
            specifications.add((root, cq, cb) -> cb.equal(cb.lower(root.get("region")), region));
        }
        if (query.effectiveDateFrom() != null) {
            specifications.add((root, cq, cb) ->
                    cb.greaterThanOrEqualTo(root.get("effectiveDate"), query.effectiveDateFrom()));
        }
        if (query.effectiveDateTo() != null) {
            specifications.add((root, cq, cb) ->
                    cb.lessThanOrEqualTo(root.get("effectiveDate"), query.effectiveDateTo()));
        }

        return Specification.allOf(specifications);
    }

    private static Specification<PolicyEntity> matchesFreeText(String term) {
        String pattern = "%" + escapeLike(term.toLowerCase(Locale.ROOT)) + "%";
        return (root, cq, cb) -> cb.or(
                cb.like(cb.lower(root.get("policyNumber")), pattern, ESCAPE),
                cb.like(cb.lower(root.get("policyholderName")), pattern, ESCAPE),
                cb.like(cb.lower(root.get("underwriter")), pattern, ESCAPE));
    }

    private static String escapeLike(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
