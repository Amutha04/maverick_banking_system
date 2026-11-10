package com.springboot.BankingSystem.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record LoanApproveDto(
        @NotNull(message = "Amount is required")
        BigDecimal amount,
        @NotNull(message = "Interest rate is required")
        BigDecimal interestRate,
        @NotNull(message = "Tenure months is required")
        Integer tenureMonths
) {
}
