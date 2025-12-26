package com.leanpay.loancalculator.service;

import com.leanpay.loancalculator.cache.LoanCacheFacade;
import com.leanpay.loancalculator.dto.request.LoanCalculationRequest;
import com.leanpay.loancalculator.dto.response.LoanResponse;
import com.leanpay.loancalculator.entity.Loan;
import com.leanpay.loancalculator.mapper.LoanCalculationResponseMapper;
import com.leanpay.loancalculator.repository.LoanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceCacheTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanCalculationResponseMapper responseMapper;

    @Mock
    private AsyncLoanCreationService asyncLoanCreationService;

    @Mock
    private LoanCacheFacade cache;

    @InjectMocks
    private LoanService loanService;

    private static final LoanCalculationRequest REQUEST =
            new LoanCalculationRequest(
                    BigDecimal.valueOf(1000),
                    BigDecimal.valueOf(5),
                    12
            );

    private static final String KEY = "1000:5:12";

    @Test
    void shouldReturnCachedResponseWithoutTouchingDb() {
        LoanResponse cached = mock(LoanResponse.class);

        when(cache.generateCacheKey(REQUEST)).thenReturn(KEY);
        when(cache.getResponseFromCache(KEY))
                .thenReturn(Optional.of(cached));

        LoanResponse result = loanService.calculateLoan(REQUEST);

        assertThat(result).isSameAs(cached);

        verifyNoInteractions(
                loanRepository,
                asyncLoanCreationService,
                responseMapper
        );
    }

    @Test
    void shouldCacheFullResponseWhenLoanExistsInDb() {
        Loan loan = mock(Loan.class);
        LoanResponse fullResponse = mock(LoanResponse.class);

        when(cache.generateCacheKey(REQUEST)).thenReturn(KEY);
        when(cache.getResponseFromCache(KEY)).thenReturn(Optional.empty());
        when(loanRepository.findByAmountAndAnnualInterestRateAndNumberOfMonths(
                REQUEST.amount(),
                REQUEST.annualInterestRate(),
                REQUEST.numberOfMonths()
        )).thenReturn(Optional.of(loan));
        when(responseMapper.toResponse(loan)).thenReturn(fullResponse);

        LoanResponse result = loanService.calculateLoan(REQUEST);

        assertThat(result).isSameAs(fullResponse);

        verify(cache).putFullResponse(KEY, fullResponse);
        verify(cache).evictStatusResponse(KEY);
        verify(asyncLoanCreationService, never()).createAndSaveLoanAsync(any());
    }

    @Test
    void shouldStartAsyncAndCacheStatusWhenLoanMissing() {
        LoanResponse status = mock(LoanResponse.class);

        when(cache.generateCacheKey(REQUEST)).thenReturn(KEY);
        when(cache.getResponseFromCache(KEY)).thenReturn(Optional.empty());
        when(loanRepository.findByAmountAndAnnualInterestRateAndNumberOfMonths(
                REQUEST.amount(),
                REQUEST.annualInterestRate(),
                REQUEST.numberOfMonths()
        )).thenReturn(Optional.empty());
        when(responseMapper.toStatusResponse(REQUEST))
                .thenReturn(status);

        LoanResponse result = loanService.calculateLoan(REQUEST);

        assertThat(result).isSameAs(status);

        verify(asyncLoanCreationService)
                .createAndSaveLoanAsync(REQUEST);
        verify(cache).putStatusResponse(KEY, status);
    }
}

