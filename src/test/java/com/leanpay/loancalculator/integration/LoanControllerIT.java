package com.leanpay.loancalculator.integration;


import com.leanpay.loancalculator.dto.LoanCalculationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class LoanControllerIT {

    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15.3")
            .withDatabaseName("loan_calculator")
            .withUsername("user")
            .withPassword("pass");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Inject the container properties into Spring Boot
    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
    }

    @Test
    void calculateLoan_happyPath_shouldPersistLoanAndReturnResponse() throws Exception {
        LoanCalculationRequest request = new LoanCalculationRequest(
                new BigDecimal("1000"),
                new BigDecimal("5.0"),
                12
        );

        mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loan.amount").value(1000))
                .andExpect(jsonPath("$.loan.annualInterestRate").value(5.0))
                .andExpect(jsonPath("$.loan.numberOfMonths").value(12))
                .andExpect(jsonPath("$.summary.monthlyPayment").exists())
                .andExpect(jsonPath("$.summary.totalPayments").exists())
                .andExpect(jsonPath("$.summary.totalInterest").exists())
                .andExpect(jsonPath("$.installmentPlan").isArray())
                .andExpect(jsonPath("$.installmentPlan.length()").value(12));
    }

    @Test
    void calculateLoan_validationError_shouldReturn422WithErrorDetails() throws Exception {
        LoanCalculationRequest invalidRequest = new LoanCalculationRequest(
                new BigDecimal("50"),   // invalid (< 100)
                new BigDecimal("2.0"),  // invalid (< 3)
                1                        // invalid (< 2)
        );

        mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(3))
                .andExpect(jsonPath("$.errors[*].field").exists())
                .andExpect(jsonPath("$.errors[*].errorMessage").exists());
    }

    // -------- EDGE CASES --------

    @Test
    void calculateLoan_minBoundaryValues_shouldSucceed() throws Exception {
        LoanCalculationRequest request = new LoanCalculationRequest(
                new BigDecimal("100.0"),
                new BigDecimal("3.0"),
                2
        );

        mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.installmentPlan.length()").value(2));
    }

    @Test
    void calculateLoan_largeLoanLongDuration_shouldSucceed() throws Exception {
        LoanCalculationRequest request = new LoanCalculationRequest(
                new BigDecimal("1000000"),
                new BigDecimal("7.5"),
                360
        );

        mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.installmentPlan.length()").value(360))
                .andExpect(jsonPath("$.summary.totalPayments").exists());
    }

    @Test
    void calculateLoan_nullFields_shouldReturn422() throws Exception {
        String payload = """
                {
                  "amount": null,
                  "annualInterestRate": null,
                  "numberOfMonths": null
                }
                """;

        mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errors.length()").value(3));
    }

    @Test
    void calculateLoan_missingFields_shouldReturn422() throws Exception {
        String payload = "{}";

        mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errors.length()").value(3));
    }

    @Test
    void calculateLoan_invalidJson_shouldReturn400() throws Exception {
        String invalidJson = "{ amount: 1000, interest: }";

        mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void calculateLoan_wrongContentType_shouldReturn415() throws Exception {
        mockMvc.perform(post("/loans")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("invalid"))
                .andExpect(status().isUnsupportedMediaType());
    }


}
