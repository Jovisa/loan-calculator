package com.leanpay.loancalculator.cache;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String FULL_RESPONSE_CACHE = "fullResponse";
    public static final String STATUS_RESPONSE_CACHE = "statusResponse";

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {

        RedisSerializationContext.SerializationPair<Object> jsonSerializer =
                RedisSerializationContext.SerializationPair.fromSerializer(
                        RedisSerializer.json()
                );

        RedisCacheConfiguration baseConfig =
                RedisCacheConfiguration.defaultCacheConfig()
                        .serializeValuesWith(jsonSerializer)
                        .disableCachingNullValues();

        RedisCacheConfiguration statusCacheConfig =
                baseConfig.entryTtl(Duration.ofSeconds(5));

        RedisCacheConfiguration fullCacheConfig =
                baseConfig.entryTtl(Duration.ofMinutes(10));

        return RedisCacheManager.builder(factory)
                .withCacheConfiguration(
                        CacheConfig.STATUS_RESPONSE_CACHE,
                        statusCacheConfig
                )
                .withCacheConfiguration(
                        CacheConfig.FULL_RESPONSE_CACHE,
                        fullCacheConfig
                )
                .build();
    }

}
