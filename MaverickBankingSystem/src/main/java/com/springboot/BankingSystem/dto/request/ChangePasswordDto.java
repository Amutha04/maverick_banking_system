package com.springboot.BankingSystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChangePasswordDto(
        @NotNull(message = "Current password is required")
        @NotBlank(message = "Current password is required")
        String currentPassword,
        @NotNull(message = "New password is required")
        @NotBlank(message = "New password is required")
        @Size(min = 6, message = "New password needs at least 6 characters")
        String newPassword
) {
}
