package com.leanpay.loancalculator.service;

import com.leanpay.loancalculator.dto.LoanCalculationRequest;
import com.leanpay.loancalculator.dto.LoanCalculationResponse;
import com.leanpay.loancalculator.entity.Loan;
import com.leanpay.loancalculator.mapper.LoanCalculationResponseMapper;
import com.leanpay.loancalculator.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final AmortizationCalculator amortizationCalculator;
    private final LoanRepository loanRepository;
    private final LoanCalculationResponseMapper responseMapper;


    public LoanCalculationResponse calculateLoan(LoanCalculationRequest request) {
        Loan loan = amortizationCalculator.calculateAndBuildLoan(request);
        Loan savedLoan = loanRepository.save(loan);
        return responseMapper.toResponse(savedLoan);
    }

}
