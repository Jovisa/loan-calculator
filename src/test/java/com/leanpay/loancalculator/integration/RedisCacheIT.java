package com.leanpay.loancalculator.integration;

import com.leanpay.loancalculator.cache.CacheConfig;
import com.leanpay.loancalculator.dto.request.LoanCalculationRequest;
import com.leanpay.loancalculator.dto.response.LoanCalculationResponse;
import com.leanpay.loancalculator.dto.response.LoanStatus;
import com.leanpay.loancalculator.service.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;


public class RedisCacheIT extends AbstractIntegrationTest{

    private static final LoanCalculationRequest REQUEST =
            new LoanCalculationRequest(
                    BigDecimal.valueOf(1000),
                    BigDecimal.valueOf(5),
                    12
            );

    private static final String KEY = "1000:5:12";

    private static final String STATUS_RESPONSE_KEY =
            CacheConfig.STATUS_RESPONSE_CACHE + "::" + KEY;

    private static final String FULL_RESPONSE_KEY =
            CacheConfig.FULL_RESPONSE_CACHE + "::" + KEY;


    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private LoanService loanService;


    @BeforeEach
    void clearRedis() {
        stringRedisTemplate.getConnectionFactory()
                .getConnection()
                .serverCommands()
                .flushAll();
    }


    @Test
    void shouldPutAndGetEntryFromRedis() {
        stringRedisTemplate.opsForValue().set("test-key", "test-value");

        String value = stringRedisTemplate.opsForValue().get("test-key");

        assertEquals("test-value", value);
    }

    @Test
    void shouldCacheStatusResponseInRedis() {

        loanService.calculateLoan(REQUEST);

        Set<String> keys = stringRedisTemplate.keys("*");
        assertFalse(keys.isEmpty());
        assertTrue(keys.contains(STATUS_RESPONSE_KEY));


        String json = stringRedisTemplate.opsForValue().get(STATUS_RESPONSE_KEY);
        assertNotNull(json);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);

        JsonNode loan = root.get("loan");

        assertEquals("CALCULATING", root.get("status").asString());
        assertEquals(12, loan.get("numberOfMonths").asInt());

        JsonNode amountNode = loan.get("amount");
        assertEquals(1000, amountNode.get(1).asInt());
    }

    @Test
    void shouldCacheFullResponseWhenFullRequestFetchedFromMainDb () throws InterruptedException {
        loanService.calculateLoan(REQUEST);

        Thread.sleep(10_000);

        LoanCalculationResponse fullResponse = (LoanCalculationResponse) loanService.calculateLoan(REQUEST);
        assertEquals(LoanStatus.DONE, fullResponse.status());

        // assert key exists
        String json = stringRedisTemplate.opsForValue().get(FULL_RESPONSE_KEY);
        assertNotNull(json);

        // assert status cache evict
        Set<String> keys = stringRedisTemplate.keys("*");
        assertFalse(keys.contains(STATUS_RESPONSE_KEY));
        assertTrue(keys.contains(FULL_RESPONSE_KEY));

        // assert fullResponse cache TTL
        Long ttl = stringRedisTemplate.getExpire(FULL_RESPONSE_KEY, TimeUnit.MINUTES);
        assertNotNull(ttl);
        assertTrue(ttl > 0);
        assertTrue(ttl <= 10);

        // assert FullResponse correct
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);

        JsonNode loan = root.get("loan");

        assertEquals("DONE", root.get("status").asString());
        assertEquals(12, loan.get("numberOfMonths").asInt());

        JsonNode amountNode = loan.get("amount");
        assertEquals(1000, amountNode.get(1).asInt());

        JsonNode installmentPlan = root.get("installmentPlan").get(1);
        assertEquals(12, installmentPlan.size());
    }

    @Test
    void shouldHaveTtlOnStatusCacheEntry() {
        loanService.calculateLoan(REQUEST);

        Long ttl = stringRedisTemplate.getExpire(STATUS_RESPONSE_KEY, TimeUnit.SECONDS);

        assertNotNull(ttl);
        assertTrue(ttl > 0);
        assertTrue(ttl <= 5);
    }

}
