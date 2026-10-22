package com.springboot.BankingSystem.dto.request;

import com.springboot.BankingSystem.enums.LoanType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record LoanDto(
        @NotNull(message = "Loan Type is required")
        LoanType loanType,
        @NotNull(message = "Amount is required")
        BigDecimal amount,
        @NotNull(message = "Tenure is required")
        Integer tenureMonths
) {
}
