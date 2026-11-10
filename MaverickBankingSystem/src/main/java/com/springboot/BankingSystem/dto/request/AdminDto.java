package com.springboot.BankingSystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminDto(
        @NotNull(message = "Username is required")
        @NotBlank(message = "Username is required")
        String username,
        @NotNull(message = "Password is required")
        @NotBlank(message = "Password is required")
        String password
) {
}
