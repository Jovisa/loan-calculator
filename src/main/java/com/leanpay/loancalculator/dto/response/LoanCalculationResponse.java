package com.leanpay.loancalculator.dto.response;

import com.leanpay.loancalculator.dto.request.LoanCalculationRequest;
import lombok.Builder;

import java.util.List;

@Builder
public record LoanCalculationResponse(
        LoanCalculationRequest loan,
        LoanStatus status,
        SummaryDto summary,
        List<InstallmentDto> installmentPlan
) implements LoanResponse { }
