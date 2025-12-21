package com.leanpay.loancalculator.repository;

import com. leanpay. loancalculator.entity.Installment;
import com.leanpay.loancalculator.entity. Loan;
import org.junit.jupiter. api.BeforeEach;
import org.junit.jupiter. api.Test;
import org.springframework. beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework. test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util. Optional;

import static org.assertj. core.api. Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class InstallmentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InstallmentRepository installmentRepository;

    @Autowired
    private LoanRepository loanRepository;

    private Loan testLoan;
    private Installment testInstallment;

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

        testInstallment = Installment.builder()
                .period(1)
                .payment(new BigDecimal("450.00"))
                .principal(new BigDecimal("404.17"))
                .interest(new BigDecimal("45.83"))
                .balance(new BigDecimal("9595.83"))
                .build();
    }

    @Test
    void save_ShouldPersistInstallmentWithGeneratedId() {
        // Given
        Loan savedLoan = loanRepository.save(testLoan);
        testInstallment.setLoan(savedLoan);

        // When
        Installment savedInstallment = installmentRepository.save(testInstallment);

        // Then
        assertThat(savedInstallment.getId()).isNotNull();
        assertThat(savedInstallment.getPeriod()).isEqualTo(1);
        assertThat(savedInstallment.getPayment()).isEqualByComparingTo(new BigDecimal("450.00"));
        assertThat(savedInstallment. getPrincipal()).isEqualByComparingTo(new BigDecimal("404.17"));
        assertThat(savedInstallment.getInterest()).isEqualByComparingTo(new BigDecimal("45.83"));
        assertThat(savedInstallment.getBalance()).isEqualByComparingTo(new BigDecimal("9595.83"));
        assertThat(savedInstallment.getLoan().getId()).isEqualTo(savedLoan.getId());
    }

    @Test
    void save_ShouldPersistMultipleInstallmentsForSameLoan() {
        // Given
        Loan savedLoan = loanRepository.save(testLoan);

        Installment installment1 = Installment.builder()
                .period(1)
                .payment(new BigDecimal("450.00"))
                .principal(new BigDecimal("404.17"))
                .interest(new BigDecimal("45.83"))
                .balance(new BigDecimal("9595.83"))
                .loan(savedLoan)
                .build();

        Installment installment2 = Installment.builder()
                .period(2)
                .payment(new BigDecimal("450.00"))
                .principal(new BigDecimal("406.02"))
                .interest(new BigDecimal("43.98"))
                .balance(new BigDecimal("9189.81"))
                .loan(savedLoan)
                .build();

        // When
        installmentRepository.save(installment1);
        installmentRepository. save(installment2);

        // Then
        List<Installment> allInstallments = installmentRepository.findAll();
        assertThat(allInstallments).hasSize(2);
        assertThat(allInstallments)
                .extracting(Installment::getPeriod)
                .containsExactlyInAnyOrder(1, 2);
        assertThat(allInstallments)
                .extracting(installment -> installment. getLoan().getId())
                .containsOnly(savedLoan. getId());
    }

    @Test
    void findById_ShouldReturnInstallmentWhenExists() {
        // Given
        Loan savedLoan = loanRepository.save(testLoan);
        testInstallment.setLoan(savedLoan);
        Installment savedInstallment = installmentRepository.save(testInstallment);

        // When
        Optional<Installment> foundInstallment = installmentRepository.findById(savedInstallment.getId());

        // Then
        assertThat(foundInstallment).isPresent();
        assertThat(foundInstallment.get().getPeriod()).isEqualTo(1);
        assertThat(foundInstallment.get().getLoan().getId()).isEqualTo(savedLoan.getId());
    }

    @Test
    void findById_ShouldReturnEmptyWhenNotExists() {
        // When
        Optional<Installment> foundInstallment = installmentRepository.findById(999L);

        // Then
        assertThat(foundInstallment).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllInstallments() {
        // Given
        Loan savedLoan = loanRepository.save(testLoan);

        Installment installment1 = Installment.builder()
                .period(1)
                .payment(new BigDecimal("450.00"))
                .principal(new BigDecimal("404.17"))
                .interest(new BigDecimal("45.83"))
                .balance(new BigDecimal("9595.83"))
                .loan(savedLoan)
                .build();

        Installment installment2 = Installment. builder()
                .period(2)
                .payment(new BigDecimal("450.00"))
                .principal(new BigDecimal("406.02"))
                .interest(new BigDecimal("43.98"))
                .balance(new BigDecimal("9189.81"))
                .loan(savedLoan)
                .build();

        installmentRepository.save(installment1);
        installmentRepository. save(installment2);

        // When
        List<Installment> allInstallments = installmentRepository.findAll();

        // Then
        assertThat(allInstallments).hasSize(2);
        assertThat(allInstallments)
                .extracting(Installment::getPeriod)
                .containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void delete_ShouldRemoveInstallment() {
        // Given
        Loan savedLoan = loanRepository.save(testLoan);
        testInstallment.setLoan(savedLoan);
        Installment savedInstallment = installmentRepository.save(testInstallment);
        Long installmentId = savedInstallment.getId();

        // When
        installmentRepository. delete(savedInstallment);
        entityManager.flush();

        // Then
        assertThat(installmentRepository.findById(installmentId)).isEmpty();
        // Verify loan still exists
        assertThat(loanRepository.findById(savedLoan.getId())).isPresent();
    }

    @Test
    void deleteById_ShouldRemoveInstallment() {
        // Given
        Loan savedLoan = loanRepository.save(testLoan);
        testInstallment.setLoan(savedLoan);
        Installment savedInstallment = installmentRepository.save(testInstallment);
        Long installmentId = savedInstallment.getId();

        // When
        installmentRepository.deleteById(installmentId);

        // Then
        assertThat(installmentRepository.findById(installmentId)).isEmpty();
    }

    @Test
    void count_ShouldReturnCorrectCount() {
        // Given
        assertThat(installmentRepository. count()).isEqualTo(0);

        Loan savedLoan = loanRepository.save(testLoan);
        testInstallment.setLoan(savedLoan);

        // When
        installmentRepository.save(testInstallment);

        // Then
        assertThat(installmentRepository.count()).isEqualTo(1);
    }

    @Test
    void existsById_ShouldReturnTrueWhenExists() {
        // Given
        Loan savedLoan = loanRepository.save(testLoan);
        testInstallment.setLoan(savedLoan);
        Installment savedInstallment = installmentRepository.save(testInstallment);

        // When/Then
        assertThat(installmentRepository.existsById(savedInstallment. getId())).isTrue();
        assertThat(installmentRepository.existsById(999L)).isFalse();
    }

    @Test
    void update_ShouldModifyInstallment() {
        // Given
        Loan savedLoan = loanRepository.save(testLoan);
        testInstallment.setLoan(savedLoan);
        Installment savedInstallment = installmentRepository.save(testInstallment);

        // When
        savedInstallment.setPayment(new BigDecimal("500.00"));
        savedInstallment.setPrincipal(new BigDecimal("454.17"));
        Installment updatedInstallment = installmentRepository.save(savedInstallment);

        // Then
        assertThat(updatedInstallment. getPayment()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(updatedInstallment.getPrincipal()).isEqualByComparingTo(new BigDecimal("454.17"));
    }

    @Test
    void saveAll_ShouldPersistMultipleInstallments() {
        // Given
        Loan savedLoan = loanRepository.save(testLoan);

        List<Installment> installments = List.of(
                Installment.builder()
                        . period(1)
                        .payment(new BigDecimal("450.00"))
                        .principal(new BigDecimal("404.17"))
                        .interest(new BigDecimal("45.83"))
                        .balance(new BigDecimal("9595.83"))
                        .loan(savedLoan)
                        .build(),
                Installment.builder()
                        .period(2)
                        .payment(new BigDecimal("450.00"))
                        .principal(new BigDecimal("406.02"))
                        .interest(new BigDecimal("43.98"))
                        .balance(new BigDecimal("9189.81"))
                        .loan(savedLoan)
                        .build()
        );

        // When
        List<Installment> savedInstallments = installmentRepository.saveAll(installments);

        // Then
        assertThat(savedInstallments).hasSize(2);
        assertThat(savedInstallments)
                .extracting(Installment:: getId)
                .allMatch(id -> id != null);
        assertThat(installmentRepository.count()).isEqualTo(2);
    }

    @Test
    void findAllById_ShouldReturnSpecificInstallments() {
        // Given
        Loan savedLoan = loanRepository.save(testLoan);

        Installment installment1 = installmentRepository.save(Installment.builder()
                .period(1)
                .payment(new BigDecimal("450.00"))
                .principal(new BigDecimal("404.17"))
                .interest(new BigDecimal("45.83"))
                .balance(new BigDecimal("9595.83"))
                .loan(savedLoan)
                .build());

        Installment installment2 = installmentRepository.save(Installment.builder()
                .period(2)
                .payment(new BigDecimal("450.00"))
                .principal(new BigDecimal("406.02"))
                .interest(new BigDecimal("43.98"))
                .balance(new BigDecimal("9189.81"))
                .loan(savedLoan)
                .build());

        // When
        List<Installment> foundInstallments = installmentRepository.findAllById(
                List.of(installment1.getId(), installment2.getId())
        );

        // Then
        assertThat(foundInstallments).hasSize(2);
        assertThat(foundInstallments)
                .extracting(Installment::getPeriod)
                .containsExactlyInAnyOrder(1, 2);
    }
}