package com.leanpay.loancalculator.service;

import com.leanpay.loancalculator.dto.LoanCalculationRequest;
import com.leanpay.loancalculator.entity.Installment;
import com.leanpay.loancalculator.entity.Loan;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class AmortizationCalculator {

    public Loan calculateAndBuildLoan(LoanCalculationRequest request) {
        BigDecimal monthlyPayment = calculateMonthlyPayment(request);
        BigDecimal monthlyInterestRate = getMonthlyInterestRate(request.annualInterestRate());

        List<Installment> installments = generateInstallments(request, monthlyInterestRate, monthlyPayment);

        BigDecimal totalPaymentAmount = calculateTotalPayments(installments);
        BigDecimal totalInterestAmount = calculateTotalInterest(totalPaymentAmount, request.amount());

        Loan loan = Loan.builder()
                .amount(request.amount())
                .annualInterestRate(request.annualInterestRate())
                .numberOfMonths(request.numberOfMonths())
                .monthlyPayment(monthlyPayment)
                .totalPayments(totalPaymentAmount)
                .totalInterest(totalInterestAmount)
                .build();

        loan.addInstallments(installments);

        return loan;
    }

    private BigDecimal calculateTotalPayments(List<Installment> installmentPlan) {
        return installmentPlan.stream()
                .map(Installment::getPayment)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTotalInterest(BigDecimal totalPayments, BigDecimal principal) {
        return totalPayments.subtract(principal)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateMonthlyPayment(LoanCalculationRequest loanRequest) {
        BigDecimal principal = loanRequest.amount();
        BigDecimal annualInterestRate = loanRequest.annualInterestRate();
        Integer months = loanRequest.numberOfMonths();

        BigDecimal monthlyRate = annualInterestRate
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

        BigDecimal onePlusRatePowerN = BigDecimal.ONE.add(monthlyRate).pow(months);

        BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRatePowerN);
        BigDecimal denominator = onePlusRatePowerN.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal getMonthlyInterestRate(BigDecimal annualInterestRate) {
        return annualInterestRate
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
    }

    private List<Installment> generateInstallments(LoanCalculationRequest request,
                                                   BigDecimal monthlyRate,
                                                   BigDecimal monthlyPayment) {
        BigDecimal principal = request.amount();
        Integer months = request.numberOfMonths();

        List<Installment> plan = new ArrayList<>(months);
        BigDecimal balance = principal;

        for (int period = 1; period <= months; period++) {

            BigDecimal interest = calculateInterest(balance, monthlyRate);
            BigDecimal principalPayment = calculatePrincipalPayment(monthlyPayment, interest);
            BigDecimal newBalance = balance.subtract(principalPayment)
                    .setScale(2, RoundingMode.HALF_UP);

            if (period == months && newBalance.compareTo(BigDecimal.ZERO) != 0) {
                principalPayment = balance.setScale(2, RoundingMode.HALF_UP);
                monthlyPayment = principalPayment.add(interest)
                        .setScale(2, RoundingMode.HALF_UP);
                newBalance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }

            Installment installment = Installment.builder()
                    .period(period)
                    .payment(monthlyPayment)
                    .principal(principalPayment)
                    .interest(interest)
                    .balance(newBalance)
                    .build();

            plan.add(installment);

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
