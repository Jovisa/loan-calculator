package com.leanpay.loancalculator.repository;

import com.leanpay.loancalculator.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {
}
