package com.leanpay.loancalculator.repository;

import com.leanpay.loancalculator.entity.Loan;
import com.leanpay.loancalculator.entity.LoanViewDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    @EntityGraph(attributePaths = {"installments"})
    Optional<Loan> findByAmountAndAnnualInterestRateAndNumberOfMonths(
            BigDecimal amount, BigDecimal annualInterestRate, Integer numberOfMonths
    );

    @Query(
            value = """
              select l.id as id,
                     l.amount as amount,
                     l.annualInterestRate as annualInterestRate,
                     l.numberOfMonths as numberOfMonths
              from Loan l
             """,
            countQuery = "select count(l.id) from Loan l"
    )
    Page<LoanViewDto> findAllProjected(Pageable pageable);


}
