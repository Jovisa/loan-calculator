package com.leanpay.loancalculator.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String FULL_RESPONSE_CACHE = "fullResponse";
    public static final String STATUS_RESPONSE_CACHE = "statusResponse";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager(
                FULL_RESPONSE_CACHE, STATUS_RESPONSE_CACHE);

        manager.registerCustomCache(
                FULL_RESPONSE_CACHE,
                Caffeine.newBuilder()
                        .maximumSize(10_000)
                        .expireAfterWrite(10, TimeUnit.MINUTES)
                        .build()
        );

        manager.registerCustomCache(
                STATUS_RESPONSE_CACHE,
                Caffeine.newBuilder()
                        .maximumSize(10_000)
                        .expireAfterWrite(5, TimeUnit.SECONDS)
                        .build()
        );

        return manager;
    }
}
