package com.springboot.BankingSystem.dto.response;

import java.time.Instant;

public record BeneficiaryInfoDto(
        Long id,
        String name,
        Long accountNumber,
        Instant addedOn
) {
}
