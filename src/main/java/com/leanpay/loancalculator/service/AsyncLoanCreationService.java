package com.leanpay.loancalculator.service;

import com.leanpay.loancalculator.dto.request.LoanCalculationRequest;
import com.leanpay.loancalculator.dto.response.LoanResponse;
import com.leanpay.loancalculator.dto.response.LoanStatus;
import com.leanpay.loancalculator.entity.Loan;
import com.leanpay.loancalculator.mapper.LoanCalculationResponseMapper;
import com.leanpay.loancalculator.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncLoanCreationService {

    private final AmortizationCalculator amortizationCalculator;
    private final LoanRepository loanRepository;
    private final LoanCalculationResponseMapper responseMapper;
    private final CacheManager cacheManager;

    @Async
    public void createAndSaveLoanAsync(LoanCalculationRequest request) {
        Loan loan = amortizationCalculator.calculateAndBuildLoan(request);
        try {
            Loan savedLoan = loanRepository.save(loan);
            LoanResponse response = responseMapper.toResponse(savedLoan);

            // cache only full response for now
            if(LoanStatus.DONE.equals(response.status())) {
                String cacheKey = cacheKey(request);
                Objects.requireNonNull(cacheManager.getCache("loans"))
                        .put(cacheKey, response);
            }

        } catch (DataIntegrityViolationException e) {
            log.debug("Loan already created by another thread for request {}", request);
        }
    }

    private String cacheKey(LoanCalculationRequest r) {
        return r.amount() + ":" + r.annualInterestRate() + ":" + r.numberOfMonths();
    }

}
