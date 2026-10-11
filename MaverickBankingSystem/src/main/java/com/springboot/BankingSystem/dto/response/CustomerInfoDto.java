package com.springboot.BankingSystem.dto.response;

import java.time.LocalDate;

public record CustomerInfoDto(
        String name,
        LocalDate dob,
        String email,
        Long contact,
        Long aadhaarNumber,
        String panNumber,
        String username
) {
}
