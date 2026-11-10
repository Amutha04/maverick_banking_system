package com.springboot.BankingSystem.dto.response;

import com.springboot.BankingSystem.enums.TransactionStatus;
import com.springboot.BankingSystem.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionInfoDto(
        String referenceNumber,
        TransactionType transactionType,
        TransactionStatus transactionStatus,
        BigDecimal amount,
        Long accountNumber,
        Long destinationAccount,
        BigDecimal balance,
        LocalDate timeStamp,
        String remarks
) {
}
