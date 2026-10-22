package com.springboot.BankingSystem.controller;

import com.springboot.BankingSystem.dto.request.TransactionDto;
import com.springboot.BankingSystem.dto.response.TransactionInfoDto;
import com.springboot.BankingSystem.enums.TransactionStatus;
import com.springboot.BankingSystem.enums.TransactionType;
import com.springboot.BankingSystem.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

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
    @GetMapping("/api/transaction/all")
    public List<TransactionInfoDto> getAll(@RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                           @RequestParam(name = "size", required = false, defaultValue = "20") int size,
                                           Principal principal) {
        return transactionService.getAll(page, size, principal.getName());
    }

    @GetMapping("/api/transaction/get/{accountId}")
    public List<TransactionInfoDto> getByAccount(@RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                                 @RequestParam(name = "size", required = false, defaultValue = "20") int size,
                                                 @PathVariable Long accountId,
                                                 Principal principal) {
        return transactionService.getByAccount(page, size, accountId, principal.getName());
    }

    @GetMapping("/api/transaction/{accountId}")
    public List<TransactionInfoDto> get(@RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                        @RequestParam(name = "size", required = false, defaultValue = "20") int size,
                                        @PathVariable Long accountId,
                                        Principal principal) {
        return transactionService.get(page, size, accountId, principal.getName());
    }

    @GetMapping("/api/transaction/filter")
    public List<TransactionInfoDto> dynamicTransactionFilter(@RequestParam(name = "accountNumber", required = false) Long accountNumber,
                                                             @RequestParam(name = "transactionType", required = false) TransactionType transactionType,
                                                             @RequestParam(name = "transactionStatus", required = false) TransactionStatus transactionStatus,
                                                             @RequestParam(name = "from", required = false) LocalDate from,
                                                             @RequestParam(name = "to", required = false) LocalDate to,
                                                             @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                                             @RequestParam(name = "size", required = false, defaultValue = "20") int size,
                                                             Principal principal) {
        return transactionService.dynamicTransactionFilter(accountNumber, transactionType, transactionStatus, from, to, page, size, principal.getName());
    }

}
