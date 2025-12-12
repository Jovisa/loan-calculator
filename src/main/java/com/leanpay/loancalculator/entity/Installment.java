package com.leanpay.loancalculator.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Installment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer period;
    private BigDecimal payment;
    private BigDecimal principal;
    private BigDecimal interest;
    private BigDecimal balance;

    @ManyToOne
    @JoinColumn(name = "loan_id")
    private Loan loan;
}

