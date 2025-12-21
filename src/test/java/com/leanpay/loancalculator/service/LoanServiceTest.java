package com.leanpay.loancalculator.service;

import com.leanpay.loancalculator.dto.LoanCalculationRequest;
import com.leanpay.loancalculator.dto.LoanCalculationResponse;
import com.leanpay.loancalculator.dto.SummaryDto;
import com.leanpay.loancalculator.entity.Loan;
import com.leanpay.loancalculator.mapper.LoanMapper;
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
    private LoanMapper responseMapper;

    @InjectMocks
    private LoanService loanService;

    @Test
    void calculateLoan_whenLoanDoesNotExist_shouldCalculateSaveAndReturnResponse() {
        // given
        LoanCalculationRequest request =
                new LoanCalculationRequest(
                        BigDecimal.valueOf(1000),
                        BigDecimal.valueOf(5),
                        10
                );

        Loan calculatedLoan = Loan.builder()
                .amount(request.amount())
                .annualInterestRate(request.annualInterestRate())
                .numberOfMonths(request.numberOfMonths())
                .build();

        Loan savedLoan = Loan.builder()
                .id(1L)
                .amount(request.amount())
                .annualInterestRate(request.annualInterestRate())
                .numberOfMonths(request.numberOfMonths())
                .build();

        LoanCalculationResponse expectedResponse =
                new LoanCalculationResponse(
                        request,
                        new SummaryDto(
                                BigDecimal.valueOf(102.31),
                                BigDecimal.valueOf(1023.06),
                                BigDecimal.valueOf(23.06)
                        ),
                        List.of()
                );

        when(loanRepository.findByAmountAndAnnualInterestRateAndNumberOfMonths(
                request.amount(),
                request.annualInterestRate(),
                request.numberOfMonths()))
                .thenReturn(Optional.empty());

        when(amortizationCalculator.calculateAndBuildLoan(request))
                .thenReturn(calculatedLoan);

        when(loanRepository.save(calculatedLoan))
                .thenReturn(savedLoan);

        when(responseMapper.toResponse(savedLoan))
                .thenReturn(expectedResponse);

        // when
        LoanCalculationResponse actualResponse =
                loanService.calculateLoan(request);

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);

        verify(loanRepository).findByAmountAndAnnualInterestRateAndNumberOfMonths(
                request.amount(),
                request.annualInterestRate(),
                request.numberOfMonths()
        );
        verify(amortizationCalculator).calculateAndBuildLoan(request);
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
        LoanCalculationRequest request =
                new LoanCalculationRequest(
                        BigDecimal.valueOf(1000),
                        BigDecimal.valueOf(5),
                        10
                );

        Loan existingLoan = Loan.builder()
                .id(42L)
                .amount(request.amount())
                .annualInterestRate(request.annualInterestRate())
                .numberOfMonths(request.numberOfMonths())
                .build();

        LoanCalculationResponse expectedResponse =
                new LoanCalculationResponse(
                        request,
                        new SummaryDto(
                                BigDecimal.valueOf(102.31),
                                BigDecimal.valueOf(1023.06),
                                BigDecimal.valueOf(23.06)
                        ),
                        List.of()
                );

        when(loanRepository.findByAmountAndAnnualInterestRateAndNumberOfMonths(
                request.amount(),
                request.annualInterestRate(),
                request.numberOfMonths()))
                .thenReturn(Optional.of(existingLoan));

        when(responseMapper.toResponse(existingLoan))
                .thenReturn(expectedResponse);

        // when
        LoanCalculationResponse actualResponse =
                loanService.calculateLoan(request);

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);

        verify(loanRepository).findByAmountAndAnnualInterestRateAndNumberOfMonths(
                request.amount(),
                request.annualInterestRate(),
                request.numberOfMonths()
        );
        verify(responseMapper).toResponse(existingLoan);

        verifyNoInteractions(amortizationCalculator);
        verify(loanRepository, never()).save(any());

        verifyNoMoreInteractions(
                loanRepository,
                responseMapper
        );
    }

}
