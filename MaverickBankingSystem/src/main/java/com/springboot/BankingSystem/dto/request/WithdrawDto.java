package com.springboot.BankingSystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record WithdrawDepositDto(
        @NotNull(message = "Amount is Required")
        BigDecimal amount,
        @NotNull(message = "Password is required")
        @NotBlank(message = "Password is required")
        String password,
        String remarks
) {
}
