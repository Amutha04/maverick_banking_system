package com.springboot.BankingSystem.dto.response;

import com.springboot.BankingSystem.enums.JobTitle;

public record TeamMemberDto(
        Long id,
        String name,
        String email,
        Long contact,
        JobTitle jobTitle,
        long pendingRequests,
        long pendingLoans
) {
}
