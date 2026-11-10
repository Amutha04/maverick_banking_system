package com.springboot.BankingSystem.controller;

import com.springboot.BankingSystem.dto.response.LoanProductDto;
import com.springboot.BankingSystem.service.LoanProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class LoanProductController {
    private final LoanProductService loanProductService;

    @GetMapping("api/loan-product/get")
    public List<LoanProductDto> getLoanProducts() {
        return loanProductService.getLoanProducts();
    }
}
