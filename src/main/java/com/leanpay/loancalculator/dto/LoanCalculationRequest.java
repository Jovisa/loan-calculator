package com.leanpay.loancalculator.dto;

import java.math.BigDecimal;

public record LoanCalculationRequest(
        BigDecimal amount,
        BigDecimal annualInterestRate,
        Integer numberOfMonths
) { }
