package com.leanpay.loancalculator.dto.response;

import com.leanpay.loancalculator.dto.request.LoanCalculationRequest;

public record LoanStatusResponse(
        LoanCalculationRequest loan,
        LoanStatus status
) implements LoanResponse {
}
