package com.leanpay.loancalculator.dto;

import java.math.BigDecimal;

public record LoanInfoDto(
        Long id,
        BigDecimal amount,
        BigDecimal annualInterestRate,
        Integer numberOfMonths
) { }
