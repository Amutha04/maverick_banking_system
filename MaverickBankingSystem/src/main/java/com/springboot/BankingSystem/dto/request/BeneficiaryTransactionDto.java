package com.springboot.BankingSystem.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BeneficiaryTransactionDto(
        @NotNull(message = "Amount is Required")
        BigDecimal amount,
        String remarks
) {
}
