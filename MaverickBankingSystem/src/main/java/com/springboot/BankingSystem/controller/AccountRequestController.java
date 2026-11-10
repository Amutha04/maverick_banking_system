package com.springboot.BankingSystem.controller;

import com.springboot.BankingSystem.dto.request.AccountDto;
import com.springboot.BankingSystem.dto.request.AssignExecutiveDto;
import com.springboot.BankingSystem.dto.request.RejectRequestDto;
import com.springboot.BankingSystem.dto.response.AccountRequestInfoDto;
import com.springboot.BankingSystem.dto.response.PageResponseDto;
import com.springboot.BankingSystem.enums.BranchName;
import com.springboot.BankingSystem.enums.RequestStatus;
import com.springboot.BankingSystem.enums.RequestType;
import com.springboot.BankingSystem.service.AccountRequestService;
import com.springboot.BankingSystem.dto.response.StatDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AccountRequestController {

    private final AccountRequestService accountRequestService;

    @PostMapping("/api/account/request")
    public void request(@RequestBody AccountDto accountDto,
                        @RequestParam(name = "accountId", required = false) Long accountId,
                        Principal principal) throws IOException {
        accountRequestService.request(accountDto,accountId, principal.getName());
    }

    @PutMapping("/api/account/open-request/approve/{accountRequestId}")
    public void approveOpenRequest(@PathVariable Long accountRequestId,
                                   Principal principal) {
        accountRequestService.approveOpenRequest(accountRequestId, principal.getName());
    }

    @PutMapping("/api/account/request/reject/{accountRequestId}")
    public void rejectOpenRequest(@PathVariable Long accountRequestId,
                                  @Valid @RequestBody RejectRequestDto rejectRequestDto,
                                  Principal principal) {
        accountRequestService.rejectRequest(accountRequestId, rejectRequestDto, principal.getName());
    }

    @PutMapping("/api/account/close-request/approve/{accountRequestId}")
    public void approveCloseRequest(@PathVariable Long accountRequestId,
                                    Principal principal) {
        accountRequestService.approveCloseRequest(accountRequestId, principal.getName());
    }

    @PutMapping("/api/request/assign-executive/{accountRequestId}")
    public void assignExecutive(@PathVariable Long accountRequestId,
                                @Valid @RequestBody AssignExecutiveDto assignExecutiveDto,
                                Principal principal) {
        accountRequestService.assignExecutive(accountRequestId, assignExecutiveDto.executiveId(), principal.getName());
    }

    @GetMapping("/api/account-request/get")
    public PageResponseDto<AccountRequestInfoDto> getByUsername(@RequestParam(name = "requestType", required = false) RequestType requestType,
                                                                @RequestParam(name = "requestStatus", required = false) RequestStatus requestStatus,
                                                                @RequestParam(name = "requestedFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate requestedFrom,
                                                                @RequestParam(name = "requestedTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate requestedTo,
                                                                @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                                                @RequestParam(name = "size", required = false, defaultValue = "20") int size,
                                                                Principal principal) {
        return accountRequestService.getByUsername(requestType, requestStatus, requestedFrom, requestedTo, page, size, principal.getName());
    }

    @GetMapping("/api/account-request/filter")
    public PageResponseDto<AccountRequestInfoDto> dynamicAccountRequestFilter(@RequestParam(name = "branchName", required = false) BranchName branchName,
                                                                             @RequestParam(name = "requestType", required = false) RequestType requestType,
                                                                             @RequestParam(name = "requestStatus", required = false) RequestStatus requestStatus,
                                                                             @RequestParam(name = "customerName", required = false) String customerName,
                                                                             @RequestParam(name = "unassigned", required = false, defaultValue = "false") boolean unassigned,
                                                                             @RequestParam(name = "requestedFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate requestedFrom,
                                                                             @RequestParam(name = "requestedTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate requestedTo,
                                                                             @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                                                             @RequestParam(name = "size", required = false, defaultValue = "20") int size,
                                                                             Principal principal) {
        return accountRequestService.dynamicAccountRequestFilter(branchName, requestType, requestStatus, customerName, unassigned, requestedFrom, requestedTo, page, size, principal.getName());
    }

    @GetMapping("/api/account-request/stat/status")
    public StatDto getAccountRequestsByStatus(Principal principal) {
        return accountRequestService.getAccountRequestsByStatus(principal.getName());
    }
}
