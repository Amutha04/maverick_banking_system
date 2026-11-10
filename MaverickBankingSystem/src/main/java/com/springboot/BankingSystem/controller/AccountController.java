package com.springboot.BankingSystem.controller;

import com.springboot.BankingSystem.dto.request.BalanceDto;
import com.springboot.BankingSystem.dto.request.DepositDto;
import com.springboot.BankingSystem.dto.request.PasswordDto;
import com.springboot.BankingSystem.dto.request.WithdrawDto;
import com.springboot.BankingSystem.dto.response.AccountInfoDto;
import com.springboot.BankingSystem.dto.response.AccountRespDto;
import com.springboot.BankingSystem.dto.response.BalanceInfoDto;
import com.springboot.BankingSystem.dto.response.PageResponseDto;
import com.springboot.BankingSystem.enums.AccountStatus;
import com.springboot.BankingSystem.enums.AccountType;
import com.springboot.BankingSystem.enums.BranchName;
import com.springboot.BankingSystem.service.AccountService;
import com.springboot.BankingSystem.dto.response.StatDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AccountController {

    private final AccountService accountService;

    @PutMapping("/api/account/set-password/{accountId}")
    public void setPassword(@PathVariable Long accountId,
                            @Valid @RequestBody PasswordDto passwordDto,
                            Principal principal) {
        accountService.setPassword(accountId, passwordDto, principal.getName());
    }

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

    @GetMapping("/api/account/get")
    public List<AccountInfoDto> getByUsername(Principal principal) {
        return accountService.getByUsername(principal.getName());
    }

    @GetMapping("/api/account/filter")
    public PageResponseDto<AccountInfoDto> dynamicAccountFilter(@RequestParam(name = "branchName", required = false) BranchName branchName,
                                                                @RequestParam(name = "accountNumber", required = false) Long accountNumber,
                                                                @RequestParam(name = "accountType", required = false) AccountType accountType,
                                                                @RequestParam(name = "status", required = false) AccountStatus status,
                                                                @RequestParam(name = "customerName", required = false) String customerName,
                                                                @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                                                @RequestParam(name = "size", required = false, defaultValue = "20") int size,
                                                                Principal principal) {
        return accountService.dynamicAccountFilter(branchName, accountNumber, accountType, status, customerName, page, size, principal.getName());
    }

    @PutMapping("/api/account/suspend/{accountId}")
    public void suspend(@PathVariable Long accountId,
                        Principal principal) {
        accountService.suspend(accountId, principal.getName());
    }

    @PutMapping("/api/account/activate/{accountId}")
    public void activate(@PathVariable Long accountId,
                         Principal principal) {
        accountService.activate(accountId, principal.getName());
    }

    @PutMapping("/api/account/withdraw/{accountId}")
    public void withdraw(@PathVariable Long accountId,
                         @Valid @RequestBody WithdrawDto withdrawDto,
                         Principal principal) {
        accountService.withdraw(accountId, withdrawDto, principal.getName());
    }

    @PutMapping("/api/account/deposit/{accountId}")
    public void deposit(@PathVariable Long accountId,
                         @Valid @RequestBody DepositDto depositDto,
                         Principal principal) {
        accountService.deposit(accountId, depositDto, principal.getName());
    }

    @PostMapping("/api/account/check-balance")
    public BalanceInfoDto checkBalance(@Valid @RequestBody BalanceDto balanceDto,
                                       Principal principal) {
        return accountService.checkBalance(balanceDto, principal.getName());
    }

    @GetMapping("/api/account/stat/type")
    public StatDto getAccountsByType(Principal principal) {
        return accountService.getAccountsByType(principal.getName());
    }

    @GetMapping("/api/account/stat/branch")
    public StatDto getAccountsByBranch(Principal principal) {
        return accountService.getAccountsByBranch(principal.getName());
    }
}
