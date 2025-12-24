package com.leanpay.loancalculator.dto.response;

import java.math.BigDecimal;

public record InstallmentDto(
        Integer period,
        BigDecimal payment,
        BigDecimal principal,
        BigDecimal interest,
        BigDecimal balance
) { }

