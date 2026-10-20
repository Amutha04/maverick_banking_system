package com.springboot.BankingSystem.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransactionDto(
        @NotNull(message = "Amount is Required")
        BigDecimal amount,
        @NotNull(message = "Receiver Account number is required")
        Long destinationAccount,
        String remarks
) {
}
