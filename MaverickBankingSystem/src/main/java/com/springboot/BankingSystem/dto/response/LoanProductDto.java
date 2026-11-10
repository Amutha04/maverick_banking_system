package com.springboot.BankingSystem.dto.response;

import com.springboot.BankingSystem.enums.LoanType;

import java.math.BigDecimal;

public record LoanProductDto(
        Long id,
        LoanType loanType,
        BigDecimal maxAmount,
        BigDecimal interestRate,
        int tenureMonths
) {
}
