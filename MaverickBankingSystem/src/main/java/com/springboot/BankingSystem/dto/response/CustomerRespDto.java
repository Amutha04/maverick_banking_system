package com.springboot.BankingSystem.dto.response;

import com.springboot.BankingSystem.enums.Gender;

import java.time.Instant;
import java.time.LocalDate;

public record CustomerRespDto (
        String name,
        Gender gender,
        LocalDate dob,
        String address,
        String email,
        Long contact,
        Long aadhaarNumber,
        String panNumber,
        Instant updatedAt,
        String username
){
}
