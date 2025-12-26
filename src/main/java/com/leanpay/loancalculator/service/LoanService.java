package com.leanpay.loancalculator.service;

import com.leanpay.loancalculator.cache.LoanCacheFacade;
import com.leanpay.loancalculator.dto.request.LoanCalculationRequest;
import com.leanpay.loancalculator.dto.response.LoanResponse;
import com.leanpay.loancalculator.entity.Loan;
import com.leanpay.loancalculator.mapper.LoanCalculationResponseMapper;
import com.leanpay.loancalculator.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final LoanCalculationResponseMapper responseMapper;
    private final AsyncLoanCreationService asyncLoanCreationService;
    private final LoanCacheFacade cache;


    @Transactional
    public LoanResponse calculateLoan(LoanCalculationRequest request) {
        String key = cache.generateCacheKey(request);

        return cache.getResponseFromCache(key)
                .or(() -> findPersistedLoan(request)
                        .map(loan -> cacheAndReturnFullResponse(key, loan)))
                .orElseGet(() -> createLoanAndReturnStatus(key, request));
    }

    private Optional<Loan> findPersistedLoan(LoanCalculationRequest request) {
        return loanRepository.findByAmountAndAnnualInterestRateAndNumberOfMonths(
                request.amount(),
                request.annualInterestRate(),
                request.numberOfMonths()
        );
    }

    private LoanResponse cacheAndReturnFullResponse(String key, Loan loan) {
        LoanResponse response = responseMapper.toResponse(loan);
        cache.evictStatusResponse(key);
        cache.putFullResponse(key, response);
        return response;
    }

    private LoanResponse createLoanAndReturnStatus(String key, LoanCalculationRequest request) {
        asyncLoanCreationService.createAndSaveLoanAsync(request);
        LoanResponse statusResponse = responseMapper.toStatusResponse(request);
        cache.putStatusResponse(key, statusResponse);
        return statusResponse;
    }

}
