package com.hcl.chubb.policy.infrastructure.cache;

import com.hcl.chubb.policy.application.port.out.SummaryCachePort;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
public class SummaryCacheAdapter implements SummaryCachePort {

    private final CacheManager cacheManager;

    public SummaryCacheAdapter(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void evictSummary() {
        Cache cache = cacheManager.getCache(CacheNames.POLICY_SUMMARY);
        if (cache != null) {
            cache.clear();
        }
    }
}
