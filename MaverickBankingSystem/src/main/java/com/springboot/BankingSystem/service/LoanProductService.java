package com.springboot.BankingSystem.service;

import com.springboot.BankingSystem.dto.response.LoanProductDto;
import com.springboot.BankingSystem.repository.LoanProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanProductService {

    private final LoanProductRepository loanProductRepository;

    public List<LoanProductDto> getLoanProducts() {
        return loanProductRepository.getLoanProducts();
    }
}
