package com.leanpay.loancalculator.mapper;

import com.leanpay.loancalculator.dto.LoanCalculationResponse;
import com.leanpay.loancalculator.entity.Installment;
import com.leanpay.loancalculator.entity.Loan;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class LoanMapperTest {

    private final InstallmentMapper installmentMapper = new InstallmentMapper();
    private final LoanMapper mapper =
            new LoanMapper(installmentMapper);

    @Test
    void shouldMapLoanToResponse() {
        Installment installment = Installment.builder()
                .period(1)
                .payment(new BigDecimal("102.31"))
                .principal(new BigDecimal("98.14"))
                .interest(new BigDecimal("4.17"))
                .balance(new BigDecimal("901.86"))
                .build();

        Loan loan = Loan.builder()
                .amount(new BigDecimal("1000"))
                .annualInterestRate(new BigDecimal("5"))
                .numberOfMonths(10)
                .monthlyPayment(new BigDecimal("102.31"))
                .totalPayments(new BigDecimal("1023.06"))
                .totalInterest(new BigDecimal("23.06"))
                .build();

        loan.addInstallment(installment);

        LoanCalculationResponse response = mapper.toResponse(loan);

        assertThat(response.loan().amount()).isEqualByComparingTo("1000");
        assertThat(response.loan().annualInterestRate()).isEqualByComparingTo("5");
        assertThat(response.loan().numberOfMonths()).isEqualTo(10);

        assertThat(response.summary().monthlyPayment()).isEqualByComparingTo("102.31");
        assertThat(response.summary().totalPayments()).isEqualByComparingTo("1023.06");
        assertThat(response.summary().totalInterest()).isEqualByComparingTo("23.06");

        assertThat(response.installmentPlan()).hasSize(1);
        assertThat(response.installmentPlan().getFirst().period()).isEqualTo(1);
    }
}