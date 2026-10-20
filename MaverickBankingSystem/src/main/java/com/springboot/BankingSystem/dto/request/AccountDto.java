package com.springboot.BankingSystem.dto.request;

import com.springboot.BankingSystem.enums.AccountType;
import com.springboot.BankingSystem.enums.RequestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AccountDto(
        @NotNull(message = "Request Type is required")
        RequestType requestType,
        @NotNull(message = "Account Type is required")
        AccountType accountType,
        String reason,
        BigDecimal initialDeposit
) {
}
