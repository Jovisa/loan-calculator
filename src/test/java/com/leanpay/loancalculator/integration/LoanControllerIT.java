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

    //todo after async is finished: calculateLoan_sameRequestTwice_shouldReuseExistingLoan

}
