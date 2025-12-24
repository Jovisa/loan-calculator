package com.leanpay.loancalculator.mapper;

import com.leanpay.loancalculator.dto.request.LoanCalculationRequest;
import com.leanpay.loancalculator.dto.response.*;
import com.leanpay.loancalculator.entity.Loan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;



@Component
@RequiredArgsConstructor
public class LoanCalculationResponseMapper {

    private final InstallmentMapper installmentMapper;


    public LoanResponse toStatusResponse(LoanCalculationRequest request) {
        return new LoanStatusResponse(
                request,
                LoanStatus.CALCULATING
        );
    }

    public LoanResponse toResponse(Loan loan) {
        return new LoanCalculationResponse(
                buildLoanDetails(loan),
                LoanStatus.DONE,
                buildSummary(loan),
                installmentMapper.toDtoList(loan.getInstallments())
        );
    }

    public LoanCalculationRequest buildLoanDetails(Loan loan) {
        return new LoanCalculationRequest(
                loan.getAmount(),
                loan.getAnnualInterestRate(),
                loan.getNumberOfMonths()
        );
    }

    public SummaryDto buildSummary(Loan loan) {
        return new SummaryDto(
                loan.getMonthlyPayment(),
                loan.getTotalPayments(),
                loan.getTotalInterest()
        );
    }
}
