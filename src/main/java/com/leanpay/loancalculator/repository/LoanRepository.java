package com.leanpay.loancalculator.repository;

import com.leanpay.loancalculator.entity.Loan;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    @EntityGraph(attributePaths = {"installments"})
    Optional<Loan> findByAmountAndAnnualInterestRateAndNumberOfMonths(
            BigDecimal amount, BigDecimal annualInterestRate, Integer numberOfMonths
    );

}
