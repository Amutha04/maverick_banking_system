package com.springboot.BankingSystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BeneficiaryDto(
        @NotNull(message = "Name is required")
        @NotBlank(message = "Name is required")
        String name,
        @NotNull(message = "Account number is required")
        Long accountNumber
) {
}
