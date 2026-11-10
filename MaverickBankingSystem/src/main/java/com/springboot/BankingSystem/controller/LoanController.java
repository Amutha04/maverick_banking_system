package com.springboot.BankingSystem.controller;

import com.springboot.BankingSystem.dto.request.AssignExecutiveDto;
import com.springboot.BankingSystem.dto.request.LoanApproveDto;
import com.springboot.BankingSystem.dto.request.LoanDto;
import com.springboot.BankingSystem.dto.request.RejectRequestDto;
import com.springboot.BankingSystem.dto.request.RepaymentDto;
import com.springboot.BankingSystem.dto.response.LoanInfoDto;
import com.springboot.BankingSystem.dto.response.PageResponseDto;
import com.springboot.BankingSystem.enums.BranchName;
import com.springboot.BankingSystem.enums.LoanStatus;
import com.springboot.BankingSystem.enums.LoanType;
import com.springboot.BankingSystem.service.LoanService;
import com.springboot.BankingSystem.dto.response.StatDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/api/loan/request/{accountId}")
    public void request(@PathVariable Long accountId,
                        @Valid @RequestBody LoanDto loanDto,
                        Principal principal) {
        loanService.request(accountId, loanDto, principal.getName());
    }

    @PutMapping("/api/loan-request/assign-executive/{loanRequestId}")
    public void assignExecutive(@PathVariable Long loanRequestId,
                                @Valid @RequestBody AssignExecutiveDto assignExecutiveDto,
                                Principal principal) {
        loanService.assignExecutive(loanRequestId, assignExecutiveDto.executiveId(), principal.getName());
    }

    @PutMapping("/api/loan/approve/{loanId}")
    public void approve(@PathVariable Long loanId,
                        @Valid @RequestBody LoanApproveDto loanApproveDto,
                        Principal principal) {
        loanService.approve(loanId, loanApproveDto, principal.getName());
    }

    @PutMapping("/api/loan/reject/{loanId}")
    public void reject(@PathVariable Long loanId,
                       @Valid @RequestBody RejectRequestDto rejectRequestDto,
                       Principal principal) {
        loanService.reject(loanId, rejectRequestDto, principal.getName());
    }

    @PutMapping("/api/loan/verify/{loanId}")
    public void verifyLoan(@PathVariable Long loanId,
                           Principal principal){
        loanService.verifyLoan(loanId, principal.getName());
    }

    @PutMapping("/api/loan/reject-verification/{loanId}")
    public void rejectVerification(@PathVariable Long loanId,
                                   @Valid @RequestBody RejectRequestDto rejectRequestDto,
                                   Principal principal) {
        loanService.rejectVerification(loanId, rejectRequestDto, principal.getName());
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

    @PutMapping("/api/loan/disburse/{loanId}")
    public void disburse(@PathVariable Long loanId,
                       Principal principal) {
        loanService.disburse(loanId, principal.getName());
    }

    @PutMapping("/api/loan/repay/{loanId}")
    public void repay(@PathVariable Long loanId,
                      @Valid @RequestBody RepaymentDto repaymentDto,
                      Principal principal) {
        loanService.repay(loanId, repaymentDto, principal.getName());
    }

    @GetMapping("/api/loan/all")
    public List<LoanInfoDto> getAll(@RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                    @RequestParam(name = "size", required = false, defaultValue = "20") int size,
                                    Principal principal) {
        return loanService.getAll(page, size, principal.getName());
    }

    @GetMapping("/api/loan/get/{customerId}")
    public List<LoanInfoDto> getByCustomer(@RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                           @RequestParam(name = "size", required = false, defaultValue = "20") int size,
                                           @PathVariable Long customerId,
                                           Principal principal) {
        return loanService.getByCustomer(page, size, customerId, principal.getName());
    }

    @GetMapping("/api/loan/get")
    public List<LoanInfoDto> get(@RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                 @RequestParam(name = "size", required = false, defaultValue = "20") int size,
                                 Principal principal) {
        return loanService.get(page, size, principal.getName());
    }

    @GetMapping("/api/loan/{loanId}")
    public LoanInfoDto getById(@PathVariable Long loanId,
                               Principal principal) {
        return loanService.getById(loanId, principal.getName());
    }



    @GetMapping("/api/loan/filter")
    public PageResponseDto<LoanInfoDto> dynamicLoanFilter(@RequestParam(name = "branchName", required = false) BranchName branchName,
                                                          @RequestParam(name = "loanType", required = false) LoanType loanType,
                                                          @RequestParam(name = "loanStatus", required = false) LoanStatus loanStatus,
                                                          @RequestParam(name = "accountNumber", required = false) Long accountNumber,
                                                          @RequestParam(name = "customerName", required = false) String customerName,
                                                          @RequestParam(name = "unassigned", required = false, defaultValue = "false") boolean unassigned,
                                                          @RequestParam(name = "requestedFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate requestedFrom,
                                                          @RequestParam(name = "requestedTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate requestedTo,
                                                          @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                                          @RequestParam(name = "size", required = false, defaultValue = "20") int size,
                                                          Principal principal) {
        return loanService.dynamicLoanFilter(branchName, loanType, loanStatus, accountNumber, customerName, unassigned, requestedFrom, requestedTo, page, size, principal.getName());
    }

    @GetMapping("/api/loan/stat/status")
    public StatDto getLoansByStatus(Principal principal) {
        return loanService.getLoansByStatus(principal.getName());
    }
}
