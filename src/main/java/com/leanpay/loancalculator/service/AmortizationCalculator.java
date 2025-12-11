package com.leanpay.loancalculator.service;

import com.leanpay.loancalculator.dto.InstallmentDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class AmortizationCalculator {

    public BigDecimal calculateMonthlyPayment(BigDecimal principal,
                                                     BigDecimal annualInterestRate,
                                                     int months) {
        BigDecimal monthlyRate = annualInterestRate
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

        BigDecimal onePlusRatePowerN = BigDecimal.ONE.add(monthlyRate).pow(months);

        BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRatePowerN);
        BigDecimal denominator = onePlusRatePowerN.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getMonthlyInterestRate(BigDecimal annualInterestRate) {
        return annualInterestRate
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
    }

    public List<InstallmentDto> generateInstallmentPlan(
            BigDecimal principal,
            BigDecimal monthlyRate,
            int months,
            BigDecimal monthlyPayment
    ) {
        List<InstallmentDto> plan = new ArrayList<>(months);
        BigDecimal balance = principal;

        for (int period = 1; period <= months; period++) {

            BigDecimal interest = calculateInterest(balance, monthlyRate);
            BigDecimal principalPayment = calculatePrincipalPayment(monthlyPayment, interest);
            BigDecimal newBalance = balance.subtract(principalPayment).setScale(2, RoundingMode.HALF_UP);

            if (period == months && newBalance.compareTo(BigDecimal.ZERO) != 0) {
                principalPayment = balance.setScale(2, RoundingMode.HALF_UP);
                monthlyPayment = principalPayment.add(interest).setScale(2, RoundingMode.HALF_UP);
                newBalance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }

            plan.add(new InstallmentDto(
                    period,
                    monthlyPayment,
                    principalPayment,
                    interest,
                    newBalance
            ));

            balance = newBalance;
        }

        return plan;
    }

    private BigDecimal calculateInterest(BigDecimal balance, BigDecimal monthlyRate) {
        return balance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculatePrincipalPayment(BigDecimal monthlyPayment, BigDecimal interest) {
        return monthlyPayment.subtract(interest).setScale(2, RoundingMode.HALF_UP);
    }
}
