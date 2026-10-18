package com.hcl.chubb.policy.infrastructure.persistence.adapter;

import com.hcl.chubb.policy.application.port.out.PolicyCommandPort;
import com.hcl.chubb.policy.application.port.out.PolicyQueryPort;
import com.hcl.chubb.policy.application.port.out.PolicySummaryPort;
import com.hcl.chubb.policy.application.query.PageQuery;
import com.hcl.chubb.policy.application.query.PolicySearchQuery;
import com.hcl.chubb.policy.application.query.SortDirection;
import com.hcl.chubb.policy.application.result.PageResult;
import com.hcl.chubb.policy.application.result.PolicySummaryResult;
import com.hcl.chubb.policy.application.result.PremiumByLineOfBusiness;
import com.hcl.chubb.policy.domain.model.Policy;
import com.hcl.chubb.policy.domain.model.PolicyStatus;
import com.hcl.chubb.policy.infrastructure.cache.CacheNames;
import com.hcl.chubb.policy.infrastructure.persistence.entity.PolicyEntity;
import com.hcl.chubb.policy.infrastructure.persistence.mapper.PolicyEntityMapper;
import com.hcl.chubb.policy.infrastructure.persistence.repository.PolicyJpaRepository;
import com.hcl.chubb.policy.infrastructure.persistence.specification.PolicySpecifications;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@Transactional(readOnly = true)
public class PolicyPersistenceAdapter implements PolicyQueryPort, PolicyCommandPort, PolicySummaryPort {

    private final PolicyJpaRepository repository;
    private final PolicyEntityMapper mapper;

    public PolicyPersistenceAdapter(PolicyJpaRepository repository, PolicyEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public PageResult<Policy> search(PolicySearchQuery query) {
        Page<PolicyEntity> page = repository.findAll(PolicySpecifications.from(query), toPageable(query.page()));
        return new PageResult<>(
                page.getContent().stream().map(mapper::toDomain).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    @Override
    public Optional<Policy> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public Policy save(Policy policy) {
        return mapper.toDomain(repository.save(mapper.toEntity(policy)));
    }

    @Override
    @Cacheable(cacheNames = CacheNames.POLICY_SUMMARY, key = "#today.toString() + ':' + #expiringSoonDays")
    public PolicySummaryResult summarize(LocalDate today, int expiringSoonDays) {
        Map<PolicyStatus, Long> countByStatus = new EnumMap<>(PolicyStatus.class);
        for (PolicyStatus status : PolicyStatus.values()) {
            countByStatus.put(status, 0L);
        }
        repository.countGroupedByStatus().forEach(row -> countByStatus.put(row.getStatus(), row.getTotal()));
        long total = countByStatus.values().stream().mapToLong(Long::longValue).sum();

        List<PremiumByLineOfBusiness> premiums = repository.sumPremiumGroupedByLineOfBusinessAndCurrency().stream()
                .map(row -> new PremiumByLineOfBusiness(row.getLineOfBusiness(), row.getCurrency(), row.getTotal()))
                .toList();

        long expiringSoon = repository.countByStatusAndExpiryDateBetween(
                PolicyStatus.ACTIVE, today, today.plusDays(expiringSoonDays));

        return new PolicySummaryResult(
                total, Collections.unmodifiableMap(countByStatus), premiums, expiringSoon, expiringSoonDays);
    }

    private PageRequest toPageable(PageQuery page) {
        Sort.Direction direction = page.direction() == SortDirection.ASC ? Sort.Direction.ASC : Sort.Direction.DESC;
        // id is a tie-breaker so pagination is stable when the sort field has duplicates
        Sort sort = Sort.by(direction, page.sortBy().property()).and(Sort.by(Sort.Direction.ASC, "id"));
        return PageRequest.of(page.page(), page.size(), sort);
    }
}
