package com.leanpay.loancalculator.exception.dto;

public record FieldErrorDetail(
        String field,
        Object rejectedValue,
        String fieldType,
        String errorMessage
) { }
