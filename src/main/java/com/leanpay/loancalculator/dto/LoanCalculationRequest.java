package com.leanpay.loancalculator.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record LoanCalculationRequest(

        @NotNull
        @DecimalMin(value = "100.0", message = "Amount must be at least €100")
        BigDecimal amount,

        @NotNull
        @DecimalMin(value = "3.0", message = "Annual interest rate must be at least 3%")
        BigDecimal annualInterestRate,

        @NotNull
        @Min(value = 2, message = "Number of months must be at least 2")
        Integer numberOfMonths
) { }
