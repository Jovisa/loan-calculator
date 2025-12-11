package com.leanpay.loancalculator.dto;

import java.util.List;

public record LoanCalculationResponse(
        LoanCalculationRequest loan,
        SummaryDto summary,
        List<InstallmentDto> installmentPlan
) { }
