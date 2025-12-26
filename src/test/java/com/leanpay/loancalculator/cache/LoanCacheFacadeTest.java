package com.leanpay.loancalculator.cache;

import com.leanpay.loancalculator.dto.request.LoanCalculationRequest;
import com.leanpay.loancalculator.dto.response.LoanResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanCacheFacadeTest {

    @Mock
    CacheManager cacheManager;

    @Mock
    Cache fullCache;

    @Mock
    Cache statusCache;

    @InjectMocks
    LoanCacheFacade cacheFacade;

    private static final LoanCalculationRequest REQUEST =
            new LoanCalculationRequest(
                    BigDecimal.valueOf(1000),
                    BigDecimal.valueOf(5),
                    12
            );

    private static final String KEY = "1000:5:12";

    private LoanResponse fullResponse;
    private LoanResponse statusResponse;

    void mockCaches() {
        fullResponse = mock(LoanResponse.class);
        statusResponse = mock(LoanResponse.class);

        when(cacheManager.getCache(CacheConfig.FULL_RESPONSE_CACHE))
                .thenReturn(fullCache);
        when(cacheManager.getCache(CacheConfig.STATUS_RESPONSE_CACHE))
                .thenReturn(statusCache);
    }



    @Test
    void shouldReturnFullResponseAndEvictStatus() {
        mockCaches();

        when(fullCache.get(KEY, LoanResponse.class))
                .thenReturn(fullResponse);

        Optional<LoanResponse> result =
                cacheFacade.getResponseFromCache(KEY);

        assertThat(result).contains(fullResponse);

        verify(statusCache).evict(KEY);
        verifyNoMoreInteractions(statusCache);
    }

    @Test
    void shouldReturnFullResponseAndEvictStatusCache() {
        mockCaches();
        // given
        when(fullCache.get(KEY, LoanResponse.class))
                .thenReturn(fullResponse);

        // when
        Optional<LoanResponse> result =
                cacheFacade.getResponseFromCache(REQUEST);

        // then
        assertTrue(result.isPresent());
        assertEquals(fullResponse, result.get());

        verify(statusCache).evict(KEY);
        verify(statusCache, never()).get(KEY, LoanResponse.class);
    }

    @Test
    void shouldReturnStatusResponseIfFullResponseIsMissing() {
        mockCaches();
        // given
        when(fullCache.get(KEY, LoanResponse.class))
                .thenReturn(null);
        when(statusCache.get(KEY, LoanResponse.class))
                .thenReturn(statusResponse);

        // when
        Optional<LoanResponse> result =
                cacheFacade.getResponseFromCache(REQUEST);

        // then
        assertTrue(result.isPresent());
        assertEquals(statusResponse, result.get());

        verify(statusCache, never()).evict(anyString());
    }


    @Test
    void shouldReturnStatusResponseWhenFullIsMissing() {
        mockCaches();
        when(fullCache.get(KEY, LoanResponse.class))
                .thenReturn(null);
        when(statusCache.get(KEY, LoanResponse.class))
                .thenReturn(statusResponse);

        Optional<LoanResponse> result =
                cacheFacade.getResponseFromCache(KEY);

        assertThat(result).contains(statusResponse);

        verify(statusCache, never()).evict(any());
    }

    @Test
    void shouldReturnEmptyWhenNothingInCache() {
        mockCaches();
        when(fullCache.get(KEY, LoanResponse.class)).thenReturn(null);
        when(statusCache.get(KEY, LoanResponse.class)).thenReturn(null);

        Optional<LoanResponse> result =
                cacheFacade.getResponseFromCache(KEY);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldGenerateStableCacheKey() {
        String key = cacheFacade.generateCacheKey(REQUEST);
        assertEquals(KEY, key);
    }

}