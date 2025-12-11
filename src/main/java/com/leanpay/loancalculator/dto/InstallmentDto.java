package com.leanpay.loancalculator.dto;

import java.math.BigDecimal;

public record InstallmentDto(
        int period,
        BigDecimal payment,
        BigDecimal principal,
        BigDecimal interest,
        BigDecimal balance
) {}

