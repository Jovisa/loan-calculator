package com.leanpay.loancalculator.service;

import com.leanpay.loancalculator.dto.request.LoanCalculationRequest;
import com.leanpay.loancalculator.entity.Loan;
import com.leanpay.loancalculator.repository.LoanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsyncLoanCreationServiceTest {

    @Mock
    AmortizationCalculator amortizationCalculator;

    @Mock
    LoanRepository loanRepository;

    @InjectMocks
    AsyncLoanCreationService service;

    private static final LoanCalculationRequest REQUEST =
            new LoanCalculationRequest(
                    BigDecimal.valueOf(1000),
                    BigDecimal.valueOf(5),
                    12
            );

    @Test
    void shouldIgnoreDataIntegrityViolationWhenSavingLoan() {
        // given
        Loan loan = mock(Loan.class);

        when(amortizationCalculator.calculateAndBuildLoan(REQUEST))
                .thenReturn(loan);

        doThrow(new DataIntegrityViolationException("duplicate"))
                .when(loanRepository)
                .save(loan);

        // when + then (no exception should be thrown)
        assertDoesNotThrow(() ->
                service.createAndSaveLoanAsync(REQUEST)
        );

        // verify interactions
        verify(amortizationCalculator)
                .calculateAndBuildLoan(REQUEST);

        verify(loanRepository)
                .save(loan);
    }

}
