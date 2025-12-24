package com.leanpay.loancalculator.mapper;

import com.leanpay.loancalculator.dto.response.InstallmentDto;
import com.leanpay.loancalculator.entity.Installment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InstallmentMapper {

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
