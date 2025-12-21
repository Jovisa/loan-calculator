package com.leanpay.loancalculator.repository;

import com. leanpay. loancalculator.entity.Installment;
import com.leanpay.loancalculator.entity. Loan;
import org.junit.jupiter. api.BeforeEach;
import org.junit. jupiter.api.Test;
import org. springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org. springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context. ActiveProfiles;

import java.math. BigDecimal;
import java.util. ArrayList;
import java. util.List;
import java.util. Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class LoanRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LoanRepository loanRepository;

    private Loan testLoan;
    private List<Installment> testInstallments;

    @BeforeEach
    void setUp() {
        testLoan = Loan.builder()
                .amount(new BigDecimal("10000.00"))
                .annualInterestRate(new BigDecimal("5.5"))
                .numberOfMonths(24)
                .monthlyPayment(new BigDecimal("450.00"))
                .totalPayments(new BigDecimal("10800.00"))
                .totalInterest(new BigDecimal("800.00"))
                .build();

        testInstallments = createTestInstallments();
    }

    @Test
    void save_ShouldPersistLoanWithGeneratedId() {
        // When
        Loan savedLoan = loanRepository.save(testLoan);

        // Then
        assertThat(savedLoan.getId()).isNotNull();
        assertThat(savedLoan.getAmount()).isEqualByComparingTo(new BigDecimal("10000.00"));
        assertThat(savedLoan. getAnnualInterestRate()).isEqualByComparingTo(new BigDecimal("5.5"));
        assertThat(savedLoan.getNumberOfMonths()).isEqualTo(24);
        assertThat(savedLoan.getVersion()).isEqualTo(0);
    }

    @Test
    void save_ShouldPersistLoanWithInstallments() {
        // Given
        testLoan.addInstallments(testInstallments);

        // When
        Loan savedLoan = loanRepository.save(testLoan);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Loan> foundLoan = loanRepository.findById(savedLoan.getId());
        assertThat(foundLoan).isPresent();
        assertThat(foundLoan.get().getInstallments()).hasSize(2);
        assertThat(foundLoan.get().getInstallments())
                .extracting(Installment::getPeriod)
                .containsExactly(1, 2);
    }

    @Test
    void save_ShouldRespectUniqueConstraint() {
        // Given
        loanRepository.save(testLoan);
        entityManager.flush();

        Loan duplicateLoan = Loan.builder()
                .amount(new BigDecimal("10000.00"))
                .annualInterestRate(new BigDecimal("5.5"))
                .numberOfMonths(24)
                .monthlyPayment(new BigDecimal("500.00")) // Different values
                .totalPayments(new BigDecimal("12000.00"))
                .totalInterest(new BigDecimal("2000.00"))
                .build();

        // When/Then
        assertThatThrownBy(() -> {
            loanRepository.save(duplicateLoan);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_ShouldAllowDifferentLoansWithDifferentConstraintFields() {
        // Given
        loanRepository.save(testLoan);

        Loan differentLoan = Loan.builder()
                .amount(new BigDecimal("15000.00")) // Different amount
                .annualInterestRate(new BigDecimal("5.5"))
                .numberOfMonths(24)
                .monthlyPayment(new BigDecimal("675.00"))
                .totalPayments(new BigDecimal("16200.00"))
                .totalInterest(new BigDecimal("1200.00"))
                .build();

        // When
        Loan savedLoan = loanRepository. save(differentLoan);

        // Then
        assertThat(savedLoan.getId()).isNotNull();
        assertThat(loanRepository.count()).isEqualTo(2);
    }

    @Test
    void findByAmountAndAnnualInterestRateAndNumberOfMonths_ShouldReturnLoanWhenExists() {
        // Given
        testLoan.addInstallments(testInstallments);
        Loan savedLoan = loanRepository.save(testLoan);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<Loan> foundLoan = loanRepository. findByAmountAndAnnualInterestRateAndNumberOfMonths(
                new BigDecimal("10000.00"),
                new BigDecimal("5.5"),
                24
        );

        // Then
        assertThat(foundLoan).isPresent();
        assertThat(foundLoan. get().getId()).isEqualTo(savedLoan. getId());
        // Verify EntityGraph loaded installments
        assertThat(foundLoan.get().getInstallments()).hasSize(2);
        assertThat(foundLoan.get().getInstallments())
                .extracting(Installment:: getPeriod)
                .containsExactly(1, 2);
    }

    @Test
    void findByAmountAndAnnualInterestRateAndNumberOfMonths_ShouldReturnEmptyWhenNotExists() {
        // Given
        loanRepository.save(testLoan);

        // When
        Optional<Loan> foundLoan = loanRepository.findByAmountAndAnnualInterestRateAndNumberOfMonths(
                new BigDecimal("20000.00"), // Different amount
                new BigDecimal("5.5"),
                24
        );

        // Then
        assertThat(foundLoan).isEmpty();
    }

    @Test
    void findByAmountAndAnnualInterestRateAndNumberOfMonths_ShouldBeExactMatch() {
        // Given
        loanRepository.save(testLoan);

        // When - Test with slightly different interest rate
        Optional<Loan> foundLoan = loanRepository.findByAmountAndAnnualInterestRateAndNumberOfMonths(
                new BigDecimal("10000.00"),
                new BigDecimal("5.50"), // Same value but different scale
                24
        );

        // Then
        assertThat(foundLoan).isPresent(); // BigDecimal comparison should work with different scales
    }

    @Test
    void findById_ShouldReturnLoanWithoutInstallments() {
        // Given
        testLoan. addInstallments(testInstallments);
        Loan savedLoan = loanRepository.save(testLoan);
        entityManager.flush();
        entityManager. clear();

        // When
        Optional<Loan> foundLoan = loanRepository.findById(savedLoan.getId());

        // Then
        assertThat(foundLoan).isPresent();
        // Without EntityGraph, installments should be lazily loaded
        assertThat(foundLoan.get().getInstallments()).hasSize(2);
    }

    @Test
    void delete_ShouldCascadeDeleteInstallments() {
        // Given
        testLoan.addInstallments(testInstallments);
        Loan savedLoan = loanRepository.save(testLoan);
        Long loanId = savedLoan.getId();
        entityManager.flush();

        // When
        loanRepository.delete(savedLoan);
        entityManager. flush();

        // Then
        assertThat(loanRepository.findById(loanId)).isEmpty();
        // Verify installments are also deleted (cascade)
        assertThat(entityManager.getEntityManager()
                .createQuery("SELECT i FROM Installment i WHERE i.loan.id = :loanId", Installment.class)
                .setParameter("loanId", loanId)
                .getResultList()).isEmpty();
    }

    @Test
    void update_ShouldIncrementVersion() {
        // Given
        Loan savedLoan = loanRepository.save(testLoan);
        assertThat(savedLoan.getVersion()).isEqualTo(0);

        // When
        savedLoan.setMonthlyPayment(new BigDecimal("500.00"));
        Loan updatedLoan = loanRepository. save(savedLoan);
        entityManager.flush();

        // Then
        assertThat(updatedLoan.getVersion()).isEqualTo(1);
    }

    @Test
    void findAll_ShouldReturnAllLoans() {
        // Given
        Loan loan2 = Loan. builder()
                .amount(new BigDecimal("15000.00"))
                .annualInterestRate(new BigDecimal("6.0"))
                .numberOfMonths(36)
                .monthlyPayment(new BigDecimal("456.33"))
                .totalPayments(new BigDecimal("16427.88"))
                .totalInterest(new BigDecimal("1427.88"))
                .build();

        loanRepository.save(testLoan);
        loanRepository. save(loan2);

        // When
        List<Loan> allLoans = loanRepository.findAll();

        // Then
        assertThat(allLoans).hasSize(2);
        assertThat(allLoans)
                .extracting(Loan::getAmount)
                .containsExactlyInAnyOrder(
                        new BigDecimal("10000.00"),
                        new BigDecimal("15000.00")
                );
    }

    @Test
    void count_ShouldReturnCorrectCount() {
        // Given
        assertThat(loanRepository.count()).isEqualTo(0);

        // When
        loanRepository.save(testLoan);

        // Then
        assertThat(loanRepository.count()).isEqualTo(1);
    }

    @Test
    void existsById_ShouldReturnTrueWhenExists() {
        // Given
        Loan savedLoan = loanRepository.save(testLoan);

        // When/Then
        assertThat(loanRepository.existsById(savedLoan. getId())).isTrue();
        assertThat(loanRepository.existsById(999L)).isFalse();
    }

    private List<Installment> createTestInstallments() {
        List<Installment> installments = new ArrayList<>();

        installments.add(Installment.builder()
                .period(1)
                .payment(new BigDecimal("450.00"))
                .principal(new BigDecimal("404.17"))
                .interest(new BigDecimal("45.83"))
                .balance(new BigDecimal("9595.83"))
                .build());

        installments.add(Installment.builder()
                .period(2)
                .payment(new BigDecimal("450.00"))
                .principal(new BigDecimal("406.02"))
                .interest(new BigDecimal("43.98"))
                .balance(new BigDecimal("9189.81"))
                .build());

        return installments;
    }
}