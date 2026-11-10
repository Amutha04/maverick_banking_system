package com.springboot.BankingSystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectRequestDto(
        @NotBlank(message = "Rejection reason is required")
        @Size(max = 500, message = "Reason must be within 500 characters")
        String reason
) {
}
