package com.springboot.BankingSystem.controller;

import com.springboot.BankingSystem.dto.request.AccountDto;
import com.springboot.BankingSystem.dto.request.RejectRequestDto;
import com.springboot.BankingSystem.dto.response.AccountInfoDto;
import com.springboot.BankingSystem.dto.response.AccountRespDto;
import com.springboot.BankingSystem.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/api/account/{accountId}")
    public AccountRespDto get(@PathVariable Long accountId,
                              Principal principal) {
        return accountService.get(accountId, principal.getName());
    }

    @GetMapping("/api/account/all")
    public List<AccountInfoDto> getAll(@RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                       @RequestParam(name = "size", required = false, defaultValue = "20") int size,
                                       Principal principal) {
        return accountService.getAll(page, size, principal.getName());
    }

    @GetMapping("/api/account/get/{customerId}")
    public List<AccountInfoDto> getByCustomer(@RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                              @RequestParam(name = "size", required = false, defaultValue = "20") int size,
                                              @PathVariable Long customerId,
                                              Principal principal) {
        return accountService.getByCustomer(page, size, customerId, principal.getName());
    }

    @PutMapping("/api/account/{accountId}")
    public void suspend(@PathVariable Long accountId,
                        Principal principal) {
        accountService.suspend(accountId, principal.getName());
    }

}
