package com.springboot.BankingSystem.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RepaymentDto(
        @NotNull(message = "Amount is required")
        BigDecimal amount
) {
}
