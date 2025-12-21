package com.leanpay.loancalculator.entity;

import java.math.BigDecimal;

public interface LoanViewDto {
    Long getId();
    BigDecimal getAmount();
    BigDecimal getAnnualInterestRate();
    Integer getNumberOfMonths();
}
