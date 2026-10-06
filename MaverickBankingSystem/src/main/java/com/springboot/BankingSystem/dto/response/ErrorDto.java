package com.springboot.BankingSystem.dto.response;

import java.time.Instant;

public record ErrorDto(
        String message,
        String comments,
        Instant timeStamp
) {
}
