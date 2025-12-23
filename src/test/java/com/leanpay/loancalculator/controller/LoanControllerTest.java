package com.leanpay.loancalculator.controller;


import com.leanpay.loancalculator.dto.LoanCalculationRequest;
import com.leanpay.loancalculator.dto.LoanCalculationResponse;
import com.leanpay.loancalculator.dto.SummaryDto;
import com.leanpay.loancalculator.exception.GlobalExceptionHandler;
import com.leanpay.loancalculator.service.LoanService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.stream.Stream;

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


    // --- Happy path ---
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

    // --- Parameterized invalid requests ---
    record InvalidRequestCase(String name, String jsonPayload, int expectedStatus, int expectedErrorCount) {}

    static Stream<InvalidRequestCase> invalidRequestCases() {
        return Stream.of(
                new InvalidRequestCase("missing fields", "{}", 422, 3),
                new InvalidRequestCase("null fields", """
                    {
                      "amount": null,
                      "annualInterestRate": null,
                      "numberOfMonths": null
                    }
                    """, 422, 3),
                new InvalidRequestCase("invalid values", """
                    {
                      "amount": 50,
                      "annualInterestRate": 0,
                      "numberOfMonths": 1
                    }
                    """, 422, 3)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidRequestCases")
    void calculateLoan_invalidRequests_shouldReturnValidationErrors(
            InvalidRequestCase testCase
    ) throws Exception {
        mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testCase.jsonPayload()))
                .andExpect(status().is(testCase.expectedStatus()));

        if (testCase.expectedErrorCount() > 0) {
            mockMvc.perform(post("/loans")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(testCase.jsonPayload()))
                    .andExpect(jsonPath("$.errors.length()").value(testCase.expectedErrorCount()));
        }
    }

}
