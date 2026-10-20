package com.springboot.BankingSystem.dto.response;

import com.springboot.BankingSystem.enums.*;

import java.time.LocalDate;

public record AccountInfoDto(
        String name,
        Long accountNumber,
        AccountType accountType,
        double balance,
        IFSC ifscCode,
        AccountStatus status,
        LocalDate openedOn,
        String executiveName,
        String executiveEmail
) {
}
