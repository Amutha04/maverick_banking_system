package com.springboot.BankingSystem.dto.response;

import java.math.BigDecimal;

public record BalanceInfoDto(
        BigDecimal balance
) {
}
