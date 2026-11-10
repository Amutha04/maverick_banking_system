package com.springboot.BankingSystem.dto.response;

import com.springboot.BankingSystem.enums.LoanStatus;
import com.springboot.BankingSystem.enums.LoanType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanInfoDto(
        Long id,
        LoanType loanType,
        BigDecimal amount,
        BigDecimal interestRate,
        int tenureMonths,
        String purpose,
        LoanStatus loanStatus,
        BigDecimal outstandingAmount,
        LocalDate requestedOn,
        LocalDate processedOn,
        LocalDate disbursedDate,
        LocalDate closedOn,
        String remark,
        Long accountNumber,
        String customerName,
        String executiveName,
        String executiveEmail
) {
}
