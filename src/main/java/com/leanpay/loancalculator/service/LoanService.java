package com.leanpay.loancalculator.service;

import com.leanpay.loancalculator.dto.InstallmentDto;
import com.leanpay.loancalculator.dto.LoanCalculationRequest;
import com.leanpay.loancalculator.dto.LoanCalculationResponse;
import com.leanpay.loancalculator.dto.SummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final AmortizationCalculator amortizationCalculator;

    public LoanCalculationResponse calculateLoan(LoanCalculationRequest request) {

        BigDecimal monthlyPayment =
                amortizationCalculator.calculateMonthlyPayment(
                    request.amount(),
                    request.annualInterestRate(),
                    request.numberOfMonths());

        BigDecimal monthlyInterestRate = amortizationCalculator.getMonthlyInterestRate(request.annualInterestRate());

        List<InstallmentDto> installmentPlan =
                amortizationCalculator.generateInstallmentPlan(
                    request.amount(),
                    monthlyInterestRate,
                    request.numberOfMonths(),
                    monthlyPayment);

        SummaryDto summary = buildSummary(request.amount(), monthlyPayment, installmentPlan);

        return new LoanCalculationResponse(request, summary, installmentPlan);
    }

    private SummaryDto buildSummary(BigDecimal loanAmount,
                                    BigDecimal monthlyPayment,
                                    List<InstallmentDto> installmentPlan) {

        BigDecimal totalPaymentAmount = calculateTotalPayments(installmentPlan);
        BigDecimal totalInterestAmount = calculateTotalInterest(totalPaymentAmount, loanAmount);

        return new SummaryDto(monthlyPayment, totalPaymentAmount, totalInterestAmount);
    }

    private BigDecimal calculateTotalPayments(List<InstallmentDto> installmentPlan) {
        return installmentPlan.stream()
                .map(InstallmentDto::payment)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTotalInterest(BigDecimal totalPayments, BigDecimal principal) {
        return totalPayments.subtract(principal)
                .setScale(2, RoundingMode.HALF_UP);
    }

}
