package com.springboot.BankingSystem.dto.response;

import com.springboot.BankingSystem.enums.Gender;
import com.springboot.BankingSystem.enums.KYCStatus;

import java.time.LocalDate;

public record CustomerInfoDto(
        String name,
        Gender gender,
        LocalDate dob,
        String email,
        Long contact,
        Long aadhaarNumber,
        String panNumber,
        Integer cibilScore,
        KYCStatus kycStatus
) {
}
