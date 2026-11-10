package com.springboot.BankingSystem.dto.response;

import java.util.List;

public record PageResponseDto<T>(
        List<T> list,
        PaginationInfoDto paginationInfo
) {
}
