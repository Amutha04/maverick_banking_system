package com.springboot.BankingSystem.dto.response;

import com.springboot.BankingSystem.enums.JobTitle;
import com.springboot.BankingSystem.enums.Role;

import java.time.LocalDate;

public record UserDetailsDto(
        String username,
        boolean isActive,
        Role role,
        JobTitle jobTitle,
        LocalDate createdAt
) {
}
