package com.leanpay.loancalculator.dto.response;

import com.leanpay.loancalculator.dto.request.LoanCalculationRequest;

public interface LoanResponse {

    LoanCalculationRequest loan();

    LoanStatus status();
}
