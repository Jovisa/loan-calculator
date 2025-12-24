package com.leanpay.loancalculator.service;

import com.leanpay.loancalculator.dto.request.LoanCalculationRequest;
import com.leanpay.loancalculator.entity.Installment;
import com.leanpay.loancalculator.entity.Loan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AmortizationCalculatorTest {

    private AmortizationCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new AmortizationCalculator();
    }


    private static final LoanCalculationRequest REQUEST = 
            new LoanCalculationRequest(
                    BigDecimal.valueOf(1000),
                    BigDecimal.valueOf(5),
                    10);

    @Test
    void shouldCalculateLoanAndGenerateInstallmentPlan() {
        // when
        Loan loan = calculator.calculateAndBuildLoan(REQUEST);

        // then
        assertThat(loan).isNotNull();
        assertThat(loan.getAmount()).isEqualByComparingTo("1000");
        assertThat(loan.getAnnualInterestRate()).isEqualByComparingTo("5");
        assertThat(loan.getNumberOfMonths()).isEqualTo(10);

        assertThat(loan.getMonthlyPayment())
                .isEqualByComparingTo("102.31");

        assertThat(loan.getInstallments()).hasSize(10);
    }

    @Test
    void shouldSetLoanReferenceOnEachInstallment() {
        // when
        Loan loan = calculator.calculateAndBuildLoan(REQUEST);

        // then
        loan.getInstallments()
                .forEach(installment ->
                        assertThat(installment.getLoan()).isSameAs(loan)
                );
    }

    @Test
    void shouldCalculateTotalsCorrectly() {
        // when
        Loan loan = calculator.calculateAndBuildLoan(REQUEST);

        // then
        assertThat(loan.getTotalPayments())
                .isEqualByComparingTo("1023.06");

        assertThat(loan.getTotalInterest())
                .isEqualByComparingTo("23.06");
    }

    @Test
    void shouldGenerateCorrectFirstAndLastInstallment() {
        // when
        Loan loan = calculator.calculateAndBuildLoan(REQUEST);
        List<Installment> installments = loan.getInstallments();

        Installment first = installments.getFirst();
        Installment last = installments.getLast();

        // first installment
        assertThat(first.getPeriod()).isEqualTo(1);
        assertThat(first.getPayment()).isEqualByComparingTo("102.31");
        assertThat(first.getInterest()).isEqualByComparingTo("4.17");
        assertThat(first.getPrincipal()).isEqualByComparingTo("98.14");
        assertThat(first.getBalance()).isEqualByComparingTo("901.86");

        // last installment
        assertThat(last.getPeriod()).isEqualTo(10);
        assertThat(last.getBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    void shouldNotEnterLastPeriodAdjustment_whenFinalBalanceIsExactlyZero() {
        LoanCalculationRequest request = new LoanCalculationRequest(
                new BigDecimal("1000"),
                new BigDecimal("12"),   // 12% annual
                1                        // single month -> no rounding residue
        );

        Loan loan = calculator.calculateAndBuildLoan(request);

        assertEquals(1, loan.getInstallments().size());

        Installment installment = loan.getInstallments().get(0);

        // final balance is exactly zero → IF condition NOT entered
        assertEquals(BigDecimal.ZERO.setScale(2), installment.getBalance());

        // payment = principal + interest (no correction branch)
        BigDecimal expectedInterest = new BigDecimal("10.00"); // 1000 * 1%
        BigDecimal expectedPayment = new BigDecimal("1010.00");

        assertEquals(expectedInterest, installment.getInterest());
        assertEquals(expectedPayment, installment.getPayment());
    }

}