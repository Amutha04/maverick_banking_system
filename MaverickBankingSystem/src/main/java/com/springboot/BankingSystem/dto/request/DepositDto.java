package com.springboot.BankingSystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DepositDto(
        @NotNull(message = "Amount is Required")
        BigDecimal amount,
        String remarks
) {
}
