package com.leanpay.loancalculator.exception;

import com.leanpay.loancalculator.exception.dto.FieldErrorDetail;
import com.leanpay.loancalculator.exception.dto.ValidationErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {
    private final static String VALIDATION_FAILED_MESSAGE = "Validation failed";
    private final static String VALIDATION_ERROR_CODE = "VALIDATION_ERROR";
    private final static String UNKNOWN_TYPE = "Unknown";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex,
                                                                              WebRequest request) {

        List<FieldErrorDetail> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(this::mapFieldErrorToDetail)
                .toList();

        ValidationErrorResponse response = new ValidationErrorResponse(
                VALIDATION_FAILED_MESSAGE,
                VALIDATION_ERROR_CODE,
                LocalDateTime.now(),
                request.getDescription(false).replace("uri=", ""),
                errors
        );

        return ResponseEntity.unprocessableContent().body(response);
    }

    private FieldErrorDetail mapFieldErrorToDetail(FieldError fieldError) {
        return new FieldErrorDetail(
                fieldError.getField(),
                fieldError.getRejectedValue(),
                fieldError.getRejectedValue() != null
                        ? fieldError.getRejectedValue().getClass().getSimpleName()
                        : UNKNOWN_TYPE,
                fieldError.getDefaultMessage()
        );
    }

}
