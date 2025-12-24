package com.leanpay.loancalculator.service;

import com.leanpay.loancalculator.dto.request.LoanCalculationRequest;
import com.leanpay.loancalculator.dto.response.*;
import com.leanpay.loancalculator.entity.Loan;
import com.leanpay.loancalculator.mapper.LoanCalculationResponseMapper;
import com.leanpay.loancalculator.repository.LoanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanCalculationResponseMapper responseMapper;

    @Mock
    private AsyncLoanCreationService asyncLoanCreationService;

    @InjectMocks
    private LoanService loanService;



    private static final LoanCalculationRequest REQUEST =
            new LoanCalculationRequest(
                    BigDecimal.valueOf(1000),
                    BigDecimal.valueOf(5),
                    10
            );

    private static final LoanResponse STATUS_RESPONSE =
            new LoanStatusResponse(REQUEST, LoanStatus.CALCULATING);

    private static final LoanCalculationResponse RESPONSE =
            new LoanCalculationResponse(
                    REQUEST,
                    LoanStatus.DONE,
                    new SummaryDto(
                            BigDecimal.valueOf(102.31),
                            BigDecimal.valueOf(1023.06),
                            BigDecimal.valueOf(23.06)
                    ),
                    List.of()
            );


    @Test
    void calculateLoan_whenLoanDoesNotExist_shouldTriggerAsyncCreationAndReturnStatusResponse() {
        // given
        when(loanRepository.findByAmountAndAnnualInterestRateAndNumberOfMonths(
                REQUEST.amount(),
                REQUEST.annualInterestRate(),
                REQUEST.numberOfMonths()
        )).thenReturn(Optional.empty());

        when(responseMapper.toStatusResponse(REQUEST))
                .thenReturn(STATUS_RESPONSE);

        // when
        LoanResponse actualResponse =
                loanService.calculateLoan(REQUEST);

        // then
        assertThat(actualResponse).isEqualTo(STATUS_RESPONSE);

        verify(loanRepository).findByAmountAndAnnualInterestRateAndNumberOfMonths(
                REQUEST.amount(),
                REQUEST.annualInterestRate(),
                REQUEST.numberOfMonths()
        );

        verify(asyncLoanCreationService).createAndSaveLoanAsync(REQUEST);
        verify(responseMapper).toStatusResponse(REQUEST);

        verify(loanRepository, never()).save(any());

        verifyNoMoreInteractions(
                loanRepository,
                responseMapper,
                asyncLoanCreationService
        );
    }

    @Test
    void calculateLoan_whenLoanAlreadyExists_shouldReturnExistingLoanSynchronously() {
        // given
        Loan existingLoan = Loan.builder()
                .id(42L)
                .amount(REQUEST.amount())
                .annualInterestRate(REQUEST.annualInterestRate())
                .numberOfMonths(REQUEST.numberOfMonths())
                .build();

        when(loanRepository.findByAmountAndAnnualInterestRateAndNumberOfMonths(
                REQUEST.amount(),
                REQUEST.annualInterestRate(),
                REQUEST.numberOfMonths()
        )).thenReturn(Optional.of(existingLoan));

        when(responseMapper.toResponse(existingLoan))
                .thenReturn(RESPONSE);

        // when
        LoanResponse actualResponse =
                loanService.calculateLoan(REQUEST);

        // then
        assertThat(actualResponse).isEqualTo(RESPONSE);

        verify(loanRepository).findByAmountAndAnnualInterestRateAndNumberOfMonths(
                REQUEST.amount(),
                REQUEST.annualInterestRate(),
                REQUEST.numberOfMonths()
        );
        verify(responseMapper).toResponse(existingLoan);

        verifyNoInteractions(asyncLoanCreationService);
        verify(loanRepository, never()).save(any());

        verifyNoMoreInteractions(
                loanRepository,
                responseMapper
        );
    }
}

