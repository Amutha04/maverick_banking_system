package com.springboot.BankingSystem.controller;

import com.springboot.BankingSystem.dto.request.TransactionDto;
import com.springboot.BankingSystem.enums.TransactionType;
import com.springboot.BankingSystem.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/api/transaction/add/{accountId}")
    public void add(@PathVariable Long accountId,
                    @Valid @RequestBody TransactionDto transactionDto,
                    Principal principal) {
        transactionService.add(accountId, transactionDto, principal.getName());
    }
}
