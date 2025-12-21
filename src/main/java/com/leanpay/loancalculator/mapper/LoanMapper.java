package com.leanpay.loancalculator.mapper;

import com.leanpay.loancalculator.dto.LoanCalculationRequest;
import com.leanpay.loancalculator.dto.LoanCalculationResponse;
import com.leanpay.loancalculator.dto.LoanInfoDto;
import com.leanpay.loancalculator.dto.SummaryDto;
import com.leanpay.loancalculator.entity.Loan;
import com.leanpay.loancalculator.entity.LoanViewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
public class LoanMapper {

    private final InstallmentMapper installmentMapper;


    public List<LoanInfoDto> toLoanInfoList(Page<LoanViewDto> page) {
        return page.getContent().stream()
                .map(this::toLoanInfo)
                .toList();
    }

    public LoanInfoDto toLoanInfo(LoanViewDto viewDto) {
        return new LoanInfoDto(
                viewDto.getId(),
                viewDto.getAmount(),
                viewDto.getAnnualInterestRate(),
                viewDto.getNumberOfMonths()
        );
    }


    public LoanCalculationResponse toResponse(Loan loan) {
        return new LoanCalculationResponse(
                buildLoanDetails(loan),
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
