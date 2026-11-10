package com.springboot.BankingSystem.dto.request;

import com.springboot.BankingSystem.enums.LoanType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record LoanDto(
        @NotNull(message = "Loan Type is required")
        LoanType loanType,
        @NotNull(message = "Amount is required")
        BigDecimal amount,
        BigDecimal interestRate,
        @NotNull(message = "Tenure months is required")
        Integer tenureMonths,
        @NotNull(message = "Purpose is required")
        @NotBlank(message = "Purpose is required")
        String purpose
) {
}
