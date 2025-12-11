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

        BigDecimal monthlyRate = amortizationCalculator.getMonthlyInterestRate(request.annualInterestRate());

        List<InstallmentDto> plan =
                amortizationCalculator.generateInstallmentPlan(
                    request.amount(),
                    monthlyRate,
                    request.numberOfMonths(),
                    monthlyPayment);

        BigDecimal totalPayments = plan.stream()
                .map(InstallmentDto::payment)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalInterest = totalPayments.subtract(request.amount())
                .setScale(2, RoundingMode.HALF_UP);

        SummaryDto summary = new SummaryDto(
                monthlyPayment,
                totalPayments,
                totalInterest);

        return new LoanCalculationResponse(request, summary, plan);
    }

}
