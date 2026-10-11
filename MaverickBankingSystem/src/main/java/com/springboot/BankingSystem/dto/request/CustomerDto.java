package com.springboot.BankingSystem.dto.request;

import com.springboot.BankingSystem.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CustomerDto(
        @NotNull(message = "Name is required")
        @NotBlank(message = "Name is required")
        String name,
        Gender gender,
        @NotNull(message = "Date of Birth is required")
        LocalDate dob,
        @NotNull(message = "Address is required")
        @NotBlank(message = "Address is required")
        String address,
        @Email
        String email,
        @NotNull(message = "Contact is required")
        Long contact,
        @NotNull(message = "AadhaarNumber is required")
        Long aadhaarNumber,
        String panNumber,
        @NotNull(message = "Username is required")
        @NotBlank(message = "Username is required")
        String username,
        @NotNull(message = "Password is required")
        @NotBlank(message = "Password is required")
        String password
) {
}
