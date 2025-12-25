package com.leanpay.loancalculator.service;

import com.leanpay.loancalculator.dto.request.LoanCalculationRequest;
import com.leanpay.loancalculator.dto.response.LoanResponse;
import com.leanpay.loancalculator.entity.Loan;
import com.leanpay.loancalculator.mapper.LoanCalculationResponseMapper;
import com.leanpay.loancalculator.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final LoanCalculationResponseMapper responseMapper;
    private final AsyncLoanCreationService asyncLoanCreationService;

    @Cacheable(
            value = "loans",
            key = "#request.amount + ':' + #request.annualInterestRate + ':' + #request.numberOfMonths"
    )
    @Transactional
    public LoanResponse calculateLoan(LoanCalculationRequest request) {
        return getLoanIfExists(request)
                .map(responseMapper::toResponse)
                .orElseGet(() -> createAndSaveLoan(request));
    }

    private Optional<Loan> getLoanIfExists(LoanCalculationRequest request) {
        return loanRepository.findByAmountAndAnnualInterestRateAndNumberOfMonths(
                request.amount(),
                request.annualInterestRate(),
                request.numberOfMonths()
        );
    }

    private LoanResponse createAndSaveLoan(LoanCalculationRequest request) {
        asyncLoanCreationService.createAndSaveLoanAsync(request);
        return responseMapper.toStatusResponse(request);
    }

}
