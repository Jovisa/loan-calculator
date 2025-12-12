package com.leanpay.loancalculator.exception.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ValidationErrorResponse(
        String message,
        String errorCode,
        LocalDateTime timestamp,
        String path,
        List<FieldErrorDetail> errors
) { }