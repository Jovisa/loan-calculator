package com.leanpay.loancalculator.service;

import com.leanpay.loancalculator.dto.InstallmentDto;
import com.leanpay.loancalculator.dto.LoanCalculationRequest;
import com.leanpay.loancalculator.dto.LoanCalculationResponse;
import com.leanpay.loancalculator.entity.Installment;
import com.leanpay.loancalculator.entity.Loan;
import com.leanpay.loancalculator.mapper.InstallmentMapper;
import com.leanpay.loancalculator.mapper.LoanCalculationResponseMapper;
import com.leanpay.loancalculator.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final AmortizationCalculator amortizationCalculator;

    private final LoanRepository loanRepository;

    private final InstallmentMapper installmentMapper;
    private final LoanCalculationResponseMapper responseMapper;


    public LoanCalculationResponse calculateLoan(LoanCalculationRequest request) {

        // todo generate Loan entity
        BigDecimal monthlyPayment =
                amortizationCalculator.calculateMonthlyPayment(
                        request.amount(),
                        request.annualInterestRate(),
                        request.numberOfMonths());

        BigDecimal monthlyInterestRate = amortizationCalculator.getMonthlyInterestRate(request.annualInterestRate());

        List<InstallmentDto> installmentDtos =
                amortizationCalculator.generateInstallmentPlan(
                        request.amount(),
                        monthlyInterestRate,
                        request.numberOfMonths(),
                        monthlyPayment);

        BigDecimal totalPaymentAmount = calculateTotalPayments(installmentDtos);
        BigDecimal totalInterestAmount = calculateTotalInterest(totalPaymentAmount, request.amount());

        //todo build Installment list
        List<Installment> installments = installmentMapper.toEntityList(installmentDtos);

        Loan loan = Loan.builder()
                .amount(request.amount())
                .annualInterestRate(request.annualInterestRate())
                .numberOfMonths(request.numberOfMonths())
                .monthlyPayment(monthlyPayment)
                .totalPayments(totalPaymentAmount)
                .totalInterest(totalInterestAmount)
                .installments(installments)
                .build();


        //todo save Loan entity

        // Link installments to loan
        installments.forEach(i -> i.setLoan(loan));

        // Save loan (installments are saved automatically)
        loanRepository.save(loan);


        //todo generate response
        return responseMapper.toResponse(loan);
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
