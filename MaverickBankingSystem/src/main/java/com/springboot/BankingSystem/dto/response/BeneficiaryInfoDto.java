package com.springboot.BankingSystem.dto.response;

import java.time.Instant;

public record BeneficiaryInfoDto(
        String name,
        Long accountNumber,
        Instant addedOn
) {
}
