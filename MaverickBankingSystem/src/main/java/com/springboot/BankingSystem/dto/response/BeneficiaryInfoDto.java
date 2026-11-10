package com.springboot.BankingSystem.dto.response;

import com.springboot.BankingSystem.enums.BranchName;
import com.springboot.BankingSystem.enums.IFSC;

import java.time.Instant;

public record BeneficiaryInfoDto(
        Long id,
        String name,
        Long accountNumber,
        BranchName branchName,
        IFSC ifscCode,
        Instant addedOn
) {
}
