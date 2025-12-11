package com.leanpay.loancalculator.dto;

import java.math.BigDecimal;

public record SummaryDto(
        BigDecimal monthlyPayment,
        BigDecimal totalPayments,
        BigDecimal totalInterest
) { }
