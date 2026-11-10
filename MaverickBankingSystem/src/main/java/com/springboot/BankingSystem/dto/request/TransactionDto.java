package com.springboot.BankingSystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransactionDto(
        @NotNull(message = "Amount is Required")
        BigDecimal amount,
        @NotNull(message = "Receiver Account number is required")
        Long destinationAccount,
        @NotNull(message = "Password is required")
        @NotBlank(message = "Password is required")
        @Size(min = 4, max = 4, message = "Password needs exact 4 numbers")
        String password,
        String remarks
) {
}
