package com.springboot.BankingSystem.dto.response;

import com.springboot.BankingSystem.enums.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AccountInfoDto(
        Long id,
        String name,
        Long accountNumber,
        AccountType accountType,
        BigDecimal balance,
        IFSC ifscCode,
        BranchName branchName,
        District district,
        AccountStatus status,
        LocalDate openedOn,
        String executiveName,
        String executiveEmail
) {
}
