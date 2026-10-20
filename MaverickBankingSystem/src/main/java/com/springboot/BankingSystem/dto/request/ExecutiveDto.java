package com.springboot.BankingSystem.dto.request;


import com.springboot.BankingSystem.enums.JobTitle;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record ExecutiveDto(
        @NotNull(message = "Name is required")
        @NotBlank(message = "Name is required")
        String name,
        @Email
        String email,
        @NotNull(message = "Contact is required")
        Long contact,
        JobTitle jobTitle,
        @NotNull(message = "Username is required")
        @NotBlank(message = "Username is required")
        String username,
        @NotNull(message = "Password is required")
        @NotBlank(message = "Password is required")
        String password,
        @NotNull(message = "Branch ID is required")
        Long branchId
) {
}
