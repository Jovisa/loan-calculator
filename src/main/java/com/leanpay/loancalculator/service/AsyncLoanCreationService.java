package com.leanpay.loancalculator.service;

import com.leanpay.loancalculator.dto.request.LoanCalculationRequest;
import com.leanpay.loancalculator.entity.Loan;
import com.leanpay.loancalculator.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncLoanCreationService {

    private final AmortizationCalculator amortizationCalculator;
    private final LoanRepository loanRepository;

    @Async
    public void createAndSaveLoanAsync(LoanCalculationRequest request) {
        Loan loan = amortizationCalculator.calculateAndBuildLoan(request);
        try {
            loanRepository.save(loan);
        } catch (DataIntegrityViolationException e) {
            log.debug("Loan already created by another thread for request {}", request);
        }
    }

}
