package com.springboot.BankingSystem.mapper;

import com.springboot.BankingSystem.dto.response.PageResponseDto;
import com.springboot.BankingSystem.dto.response.PaginationInfoDto;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PageMapper {
    public static <T> PageResponseDto<T> convertPageToDto(Page<T> page) {
        return new PageResponseDto<>(
                page.getContent(),
                new PaginationInfoDto(
                        page.getTotalPages(),
                        page.getTotalElements(),
                        page.getNumberOfElements()
                )
        );
    }
}
