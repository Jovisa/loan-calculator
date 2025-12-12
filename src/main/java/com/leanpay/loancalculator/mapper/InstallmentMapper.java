package com.leanpay.loancalculator.mapper;

import com.leanpay.loancalculator.dto.InstallmentDto;
import com.leanpay.loancalculator.entity.Installment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InstallmentMapper {

    public Installment toEntity(InstallmentDto dto) {
        return Installment.builder()
                .period(dto.period())
                .payment(dto.payment())
                .principal(dto.principal())
                .interest(dto.interest())
                .balance(dto.balance())
                .build();
    }

    public List<Installment> toEntityList(List<InstallmentDto> dtos) {
        return dtos.stream()
                .map(this::toEntity)
                .toList();
    }

    public InstallmentDto toDto(Installment installment) {
        return new InstallmentDto(
                installment.getPeriod(),
                installment.getPayment(),
                installment.getPrincipal(),
                installment.getInterest(),
                installment.getBalance()
        );
    }

    public List<InstallmentDto> toDtoList(List<Installment> installments) {
        return installments.stream()
                .map(this::toDto)
                .toList();
    }
}
