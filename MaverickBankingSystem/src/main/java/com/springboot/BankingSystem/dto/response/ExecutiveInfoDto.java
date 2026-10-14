package com.springboot.BankingSystem.dto.response;

import com.springboot.BankingSystem.enums.JobTitle;

import java.time.Instant;

public record ExecutiveInfoDto(
        String name,
        String email,
        Long contact,
        JobTitle jobTitle,
        Instant updatedAt,
        String username
) {
}
