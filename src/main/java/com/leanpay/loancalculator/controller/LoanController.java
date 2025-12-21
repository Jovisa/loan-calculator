package com.leanpay.loancalculator.controller;

import com.leanpay.loancalculator.dto.LoanCalculationRequest;
import com.leanpay.loancalculator.dto.LoanCalculationResponse;
import com.leanpay.loancalculator.dto.LoanInfoDto;
import com.leanpay.loancalculator.entity.LoanViewDto;
import com.leanpay.loancalculator.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    public LoanCalculationResponse calculateLoan(@RequestBody @Valid LoanCalculationRequest request) {
        return loanService.calculateLoan(request);
    }

    @GetMapping
    public ResponseEntity<List<LoanInfoDto>> getAllLoansInfo(@RequestParam int pageNumber,
                                                             @RequestParam int pageSize) {
        var loansInfo = loanService.getAllLoansInfo(pageNumber, pageSize);
        return ResponseEntity.ok(loansInfo);
    }

}
