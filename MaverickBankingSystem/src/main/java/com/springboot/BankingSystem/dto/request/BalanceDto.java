package com.springboot.BankingSystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BalanceDto(
        @NotNull(message = "Account Number is required")
        Long id,
        @NotNull(message = "Password is required")
        @NotBlank(message = "Password is required")
        String password
) {
}
