package com.leanpay.loancalculator.service;

import com.leanpay.loancalculator.dto.LoanCalculationRequest;
import com.leanpay.loancalculator.dto.LoanCalculationResponse;
import com.leanpay.loancalculator.dto.SummaryDto;
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
    private AmortizationCalculator amortizationCalculator;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanCalculationResponseMapper responseMapper;

    @InjectMocks
    private LoanService loanService;


    private static final LoanCalculationRequest REQUEST =
            new LoanCalculationRequest(
                    BigDecimal.valueOf(1000),
                    BigDecimal.valueOf(5),
                    10);

    private static final LoanCalculationResponse RESPONSE =
            new LoanCalculationResponse(
                    REQUEST,
                    new SummaryDto(
                            BigDecimal.valueOf(102.31),
                            BigDecimal.valueOf(1023.06),
                            BigDecimal.valueOf(23.06)
                    ),
                    List.of()
            );



    @Test
    void calculateLoan_whenLoanDoesNotExist_shouldCalculateSaveAndReturnResponse() {
        // given
        Loan calculatedLoan = getDefaultLoanBuilder().build();

        Loan savedLoan = getDefaultLoanBuilder()
                .id(1L)
                .build();

        when(loanRepository.findByAmountAndAnnualInterestRateAndNumberOfMonths(
                REQUEST.amount(),
                REQUEST.annualInterestRate(),
                REQUEST.numberOfMonths()))
                .thenReturn(Optional.empty());

        when(amortizationCalculator.calculateAndBuildLoan(REQUEST))
                .thenReturn(calculatedLoan);

        when(loanRepository.save(calculatedLoan))
                .thenReturn(savedLoan);

        when(responseMapper.toResponse(savedLoan))
                .thenReturn(RESPONSE);

        // when
        LoanCalculationResponse actualResponse =
                loanService.calculateLoan(REQUEST);

        // then
        assertThat(actualResponse).isEqualTo(RESPONSE);

        verify(loanRepository).findByAmountAndAnnualInterestRateAndNumberOfMonths(
                REQUEST.amount(),
                REQUEST.annualInterestRate(),
                REQUEST.numberOfMonths()
        );
        verify(amortizationCalculator).calculateAndBuildLoan(REQUEST);
        verify(loanRepository).save(calculatedLoan);
        verify(responseMapper).toResponse(savedLoan);

        verifyNoMoreInteractions(
                amortizationCalculator,
                loanRepository,
                responseMapper
        );
    }

    @Test
    void calculateLoan_whenLoanAlreadyExists_shouldReturnExistingLoanWithoutSaving() {
        // given
        Loan existingLoan = getDefaultLoanBuilder()
                .id(42L)
                .build();

        when(loanRepository.findByAmountAndAnnualInterestRateAndNumberOfMonths(
                REQUEST.amount(),
                REQUEST.annualInterestRate(),
                REQUEST.numberOfMonths()))
                .thenReturn(Optional.of(existingLoan));

        when(responseMapper.toResponse(existingLoan))
                .thenReturn(RESPONSE);

        // when
        LoanCalculationResponse actualResponse =
                loanService.calculateLoan(REQUEST);

        // then
        assertThat(actualResponse).isEqualTo(RESPONSE);

        verify(loanRepository).findByAmountAndAnnualInterestRateAndNumberOfMonths(
                REQUEST.amount(),
                REQUEST.annualInterestRate(),
                REQUEST.numberOfMonths()
        );
        verify(responseMapper).toResponse(existingLoan);

        verifyNoInteractions(amortizationCalculator);
        verify(loanRepository, never()).save(any());

        verifyNoMoreInteractions(
                loanRepository,
                responseMapper
        );
    }

    private Loan.LoanBuilder getDefaultLoanBuilder() {
        return Loan.builder()
                .amount(REQUEST.amount())
                .annualInterestRate(REQUEST.annualInterestRate())
                .numberOfMonths(REQUEST.numberOfMonths());
    }

}
