package com.springboot.BankingSystem.controller;

import com.springboot.BankingSystem.dto.request.BeneficiaryDto;
import com.springboot.BankingSystem.dto.request.BeneficiaryTransactionDto;
import com.springboot.BankingSystem.dto.response.BeneficiaryInfoDto;
import com.springboot.BankingSystem.service.BeneficiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class BeneficiaryController {
    private final BeneficiaryService beneficiaryService;

    @PostMapping("/api/beneficiary/add")
    public void add(@Valid @RequestBody BeneficiaryDto beneficiaryDto,
                    Principal principal) {
        beneficiaryService.add(beneficiaryDto, principal.getName());
    }

    @DeleteMapping("/api/beneficiary/delete/{beneficiaryId}")
    public void delete(@PathVariable Long beneficiaryId,
                       Principal principal) {
        beneficiaryService.delete(beneficiaryId, principal.getName());
    }

    @GetMapping("/api/beneficiary")
    public List<BeneficiaryInfoDto> get(Principal principal) {
        return beneficiaryService.get(principal.getName());
    }

    @PostMapping("/api/beneficiary/transfer/{accountId}/{beneficiaryId}")
    public void transfer(@PathVariable Long accountId,
                         @PathVariable Long beneficiaryId,
                         @Valid @RequestBody BeneficiaryTransactionDto beneficiaryTransactionDto,
                         Principal principal) {
        beneficiaryService.transfer(accountId, beneficiaryId, beneficiaryTransactionDto, principal.getName());
    }
}
