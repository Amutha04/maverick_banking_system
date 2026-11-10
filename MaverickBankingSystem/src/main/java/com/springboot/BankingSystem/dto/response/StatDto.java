package com.springboot.BankingSystem.dto.response;

import java.util.List;

public record StatDto(
        List<String> labels,
        List<Long> values
) {
}
