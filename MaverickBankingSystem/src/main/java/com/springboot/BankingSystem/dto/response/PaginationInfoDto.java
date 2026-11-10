package com.springboot.BankingSystem.dto.response;

public record PaginationInfoDto(
        int totalPages,
        long totalRecords,
        int recordsPerPage
) {
}
