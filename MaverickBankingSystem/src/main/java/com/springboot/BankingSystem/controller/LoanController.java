package com.springboot.BankingSystem.controller;

import com.springboot.BankingSystem.dto.request.LoanDto;
import com.springboot.BankingSystem.dto.request.RejectRequestDto;
import com.springboot.BankingSystem.dto.request.RepaymentDto;
import com.springboot.BankingSystem.dto.response.LoanInfoDto;
import com.springboot.BankingSystem.enums.LoanStatus;
import com.springboot.BankingSystem.enums.LoanType;
import com.springboot.BankingSystem.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/api/loan/request/{accountId}")
    public void request(@PathVariable Long accountId,
                        @Valid @RequestBody LoanDto loanDto,
                        Principal principal) {
        loanService.request(accountId, loanDto, principal.getName());
    }

    @PutMapping("/api/loan/approve/{loanId}")
    public void approve(@PathVariable Long loanId,
                        Principal principal) {
        loanService.approve(loanId, principal.getName());
    }

    @PutMapping("/api/loan/reject/{loanId}")
    public void reject(@PathVariable Long loanId,
                       @Valid @RequestBody RejectRequestDto rejectRequestDto,
                       Principal principal) {
        loanService.reject(loanId, rejectRequestDto, principal.getName());
    }

    @PutMapping("/api/loan/accept/{loanId}")
    public void accept(@PathVariable Long loanId,
                       Principal principal) {
        loanService.accept(loanId, principal.getName());
    }

    @PutMapping("/api/loan/decline/{loanId}")
    public void decline(@PathVariable Long loanId,
                        @Valid @RequestBody RejectRequestDto rejectRequestDto,
                        Principal principal) {
        loanService.decline(loanId, rejectRequestDto, principal.getName());
    }

    @PutMapping("/api/loan/repay/{loanId}")
    public void repay(@PathVariable Long loanId,
                      @Valid @RequestBody RepaymentDto repaymentDto,
                      Principal principal) {
        loanService.repay(loanId, repaymentDto, principal.getName());
    }
}
