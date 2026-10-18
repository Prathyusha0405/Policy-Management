package com.hcl.chubb.policy.infrastructure.persistence.repository;

import com.hcl.chubb.policy.domain.model.LineOfBusiness;
import com.hcl.chubb.policy.domain.model.PolicyStatus;
import com.hcl.chubb.policy.infrastructure.persistence.entity.PolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PolicyJpaRepository extends JpaRepository<PolicyEntity, UUID>, JpaSpecificationExecutor<PolicyEntity> {

    @Query("select p.status as status, count(p) as total from PolicyEntity p group by p.status")
    List<StatusCount> countGroupedByStatus();

    @Query("""
            select p.lineOfBusiness as lineOfBusiness, p.currency as currency, sum(p.premiumAmount) as total
            from PolicyEntity p
            group by p.lineOfBusiness, p.currency
            order by p.lineOfBusiness, p.currency
            """)
    List<PremiumTotal> sumPremiumGroupedByLineOfBusinessAndCurrency();

    long countByStatusAndExpiryDateBetween(PolicyStatus status, LocalDate from, LocalDate to);

    interface StatusCount {
        PolicyStatus getStatus();

        long getTotal();
    }

    interface PremiumTotal {
        LineOfBusiness getLineOfBusiness();

        String getCurrency();

        BigDecimal getTotal();
    }
}
