package com.leanpay.loancalculator.repository;

import com.leanpay.loancalculator.entity.Installment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstallmentRepository extends JpaRepository<Installment, Long> {
}
