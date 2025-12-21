package com.leanpay.loancalculator.service;

import com.leanpay.loancalculator.dto.LoanCalculationRequest;
import com.leanpay.loancalculator.dto.LoanCalculationResponse;
import com.leanpay.loancalculator.dto.LoanInfoDto;
import com.leanpay.loancalculator.entity.Loan;
import com.leanpay.loancalculator.entity.LoanViewDto;
import com.leanpay.loancalculator.mapper.LoanMapper;
import com.leanpay.loancalculator.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final AmortizationCalculator amortizationCalculator;
    private final LoanRepository loanRepository;
    private final LoanMapper loanMapper;


    public List<LoanInfoDto> getAllLoansInfo(int pageNumber, int pageSize) {
        Pageable pageRequest = PageRequest.of(pageNumber, pageSize);
        Page<LoanViewDto> loanInfos = loanRepository.findAllProjected(pageRequest);
        return loanMapper.toLoanInfoList(loanInfos);
    }

    @Transactional
    public LoanCalculationResponse calculateLoan(LoanCalculationRequest request) {
        return getLoanIfExists(request)
                .map(loanMapper::toResponse)
                .orElseGet(() -> createAndSaveLoan(request));
    }

    private Optional<Loan> getLoanIfExists(LoanCalculationRequest request) {
        return loanRepository.findByAmountAndAnnualInterestRateAndNumberOfMonths(
                request.amount(),
                request.annualInterestRate(),
                request.numberOfMonths()
        );
    }

    private LoanCalculationResponse createAndSaveLoan(LoanCalculationRequest request) {
        Loan loan = amortizationCalculator.calculateAndBuildLoan(request);
        return loanMapper.toResponse(loanRepository.save(loan));
    }

}
