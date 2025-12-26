package com.leanpay.loancalculator.cache;

import com.leanpay.loancalculator.dto.request.LoanCalculationRequest;
import com.leanpay.loancalculator.dto.response.LoanResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LoanCacheFacade {

    private final CacheManager cacheManager;


    public Optional<LoanResponse> getResponseFromCache(LoanCalculationRequest request) {
        String key = generateCacheKey(request);

        Optional<LoanResponse> fullResponse = getFullResponse(key);
        if(fullResponse.isPresent()) {
            evictStatusResponse(key);
            return fullResponse;
        }
        return getStatusResponse(key);
    }

    public Optional<LoanResponse> getResponseFromCache(String key) {
        Optional<LoanResponse> fullResponse = getFullResponse(key);
        if(fullResponse.isPresent()) {
            evictStatusResponse(key);
            return fullResponse;
        }
        return getStatusResponse(key);
    }

    public String generateCacheKey(LoanCalculationRequest r) {
        return r.amount() + ":" + r.annualInterestRate() + ":" + r.numberOfMonths();
    }

    public Optional<LoanResponse> getFullResponse(String key) {
        return Optional.ofNullable(
                Objects.requireNonNull(cacheManager.getCache(CacheConfig.FULL_RESPONSE_CACHE))
                        .get(key, LoanResponse.class));
    }

    public void putFullResponse(String key, LoanResponse fullResponse) {
        Objects.requireNonNull(cacheManager.getCache(CacheConfig.FULL_RESPONSE_CACHE))
                .put(key, fullResponse);
    }

    public Optional<LoanResponse> getStatusResponse(String key) {
        return Optional.ofNullable(
                Objects.requireNonNull(cacheManager.getCache(CacheConfig.STATUS_RESPONSE_CACHE))
                        .get(key, LoanResponse.class));
    }

    public void putStatusResponse(String key, LoanResponse statusResponse) {
        Objects.requireNonNull(cacheManager.getCache(CacheConfig.STATUS_RESPONSE_CACHE))
                .put(key, statusResponse);
    }

    public void evictStatusResponse(String key) {
        Objects.requireNonNull(cacheManager.getCache(CacheConfig.STATUS_RESPONSE_CACHE))
                .evict(key);
    }
}
