package com.springboot.BankingSystem.dto.request;

import jakarta.validation.constraints.NotNull;

public record AssignExecutiveDto(
        @NotNull(message = "Executive ID is required")
        Long executiveId
) {
}
