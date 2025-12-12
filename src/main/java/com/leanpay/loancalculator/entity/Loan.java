package com.leanpay.loancalculator.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;
    private BigDecimal annualInterestRate;
    private Integer numberOfMonths;
    private BigDecimal monthlyPayment;
    private BigDecimal totalPayments;
    private BigDecimal totalInterest;

    //todo @EntityGraph
    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private List<Installment> installments = new ArrayList<>();


    public void addInstallments(List<Installment> installments) {
        if (installments == null || installments.isEmpty()) return;
        installments.forEach(this::addInstallment);
    }

    public void addInstallment(Installment installment) {
        if(installment == null) return;
        installment.setLoan(this);
        this.installments.add(installment);
    }
}

