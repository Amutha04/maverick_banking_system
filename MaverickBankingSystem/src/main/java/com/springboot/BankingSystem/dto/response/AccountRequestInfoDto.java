package com.springboot.BankingSystem.dto.response;

import com.springboot.BankingSystem.enums.AccountType;
import com.springboot.BankingSystem.enums.RequestStatus;
import com.springboot.BankingSystem.enums.RequestType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AccountRequestInfoDto(
        Long id,
        RequestType requestType,
        AccountType accountType,
        RequestStatus requestStatus,
        String branchName,
        String reason,
        BigDecimal initialDeposit,
        LocalDate requestedOn,
        LocalDate processedOn,
        String remark,
        Long customerId,
        String customerName,
        String executiveName,
        String executiveEmail
) {
}
