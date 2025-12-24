package com.leanpay.loancalculator.mapper;

import com.leanpay.loancalculator.dto.response.InstallmentDto;
import com.leanpay.loancalculator.entity.Installment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InstallmentMapperTest {

    private final InstallmentMapper mapper = new InstallmentMapper();

    @Test
    void shouldMapInstallmentToDto() {
        Installment installment = Installment.builder()
                .period(1)
                .payment(new BigDecimal("102.31"))
                .principal(new BigDecimal("98.14"))
                .interest(new BigDecimal("4.17"))
                .balance(new BigDecimal("901.86"))
                .build();

        InstallmentDto dto = mapper.toDto(installment);

        assertThat(dto.period()).isEqualTo(1);
        assertThat(dto.payment()).isEqualByComparingTo("102.31");
        assertThat(dto.principal()).isEqualByComparingTo("98.14");
        assertThat(dto.interest()).isEqualByComparingTo("4.17");
        assertThat(dto.balance()).isEqualByComparingTo("901.86");
    }

    @Test
    void shouldMapInstallmentListToDtoList() {
        List<Installment> installments = List.of(
                Installment.builder().period(1).build(),
                Installment.builder().period(2).build()
        );

        List<InstallmentDto> dtos = mapper.toDtoList(installments);

        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).period()).isEqualTo(1);
        assertThat(dtos.get(1).period()).isEqualTo(2);
    }
}