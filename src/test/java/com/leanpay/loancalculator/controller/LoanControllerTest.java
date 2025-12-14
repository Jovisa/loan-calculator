package com.leanpay.loancalculator.controller;


import com.leanpay.loancalculator.dto.LoanCalculationRequest;
import com.leanpay.loancalculator.dto.LoanCalculationResponse;
import com.leanpay.loancalculator.dto.SummaryDto;
import com.leanpay.loancalculator.exception.GlobalExceptionHandler;
import com.leanpay.loancalculator.service.LoanService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoanController.class)
@Import(GlobalExceptionHandler.class)
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoanService loanService;

    @Test
    void shouldCalculateLoanSuccessfully() throws Exception {
        // given
        LoanCalculationRequest request = new LoanCalculationRequest(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(5),
                10
        );

        LoanCalculationResponse response = new LoanCalculationResponse(
                request,
                new SummaryDto(
                        BigDecimal.valueOf(102.31),
                        BigDecimal.valueOf(1023.06),
                        BigDecimal.valueOf(23.06)
                ),
                List.of()
        );

        Mockito.when(loanService.calculateLoan(Mockito.any()))
                .thenReturn(response);

        // when & then
        mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loan.amount").value(1000))
                .andExpect(jsonPath("$.loan.annualInterestRate").value(5))
                .andExpect(jsonPath("$.loan.numberOfMonths").value(10))
                .andExpect(jsonPath("$.summary.monthlyPayment").value(102.31))
                .andExpect(jsonPath("$.summary.totalPayments").value(1023.06))
                .andExpect(jsonPath("$.summary.totalInterest").value(23.06));
    }

    @Test
    void shouldReturnBadRequestForInvalidInput() throws Exception {
        LoanCalculationRequest invalidRequest = new LoanCalculationRequest(
                BigDecimal.valueOf(50),   // invalid
                BigDecimal.valueOf(0),    // invalid
                1                         // invalid
        );

        mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void shouldReturnValidationErrors_whenRequestIsInvalid() throws Exception {
        String invalidRequestJson = """
            {
              "amount": 50,
              "annualInterestRate": 0,
              "numberOfMonths": 1
            }
            """;

        mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.path").value("/loans"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(3))

                // amount
                .andExpect(jsonPath("$.errors[?(@.field=='amount')]").exists())

                // annualInterestRate
                .andExpect(jsonPath("$.errors[?(@.field=='annualInterestRate')]").exists())

                // numberOfMonths
                .andExpect(jsonPath("$.errors[?(@.field=='numberOfMonths')]").exists());
    }

    @Test
    void shouldReturnValidationError_whenAmountIsNull() throws Exception {
        String requestJson = """
        {
          "annualInterestRate": 5,
          "numberOfMonths": 12
        }
        """;

        mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors[0].field").value("amount"))
                .andExpect(jsonPath("$.errors[0].rejectedValue").doesNotExist())
                .andExpect(jsonPath("$.errors[0].fieldType").value("Unknown"))
                .andExpect(jsonPath("$.errors[0].errorMessage").exists());
    }


}
