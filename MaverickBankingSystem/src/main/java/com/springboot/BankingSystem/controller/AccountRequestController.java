package com.springboot.BankingSystem.controller;

import com.springboot.BankingSystem.dto.request.AccountDto;
import com.springboot.BankingSystem.dto.request.RejectRequestDto;
import com.springboot.BankingSystem.service.AccountRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class AccountRequestController {

    private final AccountRequestService accountRequestService;

    @PostMapping("/api/account/request")
    public void request(@Valid @RequestBody AccountDto accountDto,
                        Principal principal) {
        accountRequestService.request(accountDto, principal.getName());
    }

    @PutMapping("/api/account/open-request/approve/{accountRequestId}")
    public void approveOpenRequest(@PathVariable Long accountRequestId,
                                   Principal principal) {
        accountRequestService.approveOpenRequest(accountRequestId, principal.getName());
    }

    @PutMapping("/api/account/open-request/reject/{accountRequestId}")
    public void rejectOpenRequest(@PathVariable Long accountRequestId,
                                  @Valid @RequestBody RejectRequestDto rejectRequestDto,
                                  Principal principal) {
        accountRequestService.rejectOpenRequest(accountRequestId, rejectRequestDto, principal.getName());
    }

    @PutMapping("/api/account/close-request/approve/{accountRequestId}")
    public void approveCloseRequest(@PathVariable Long accountRequestId,
                                    Principal principal) {
        accountRequestService.approveCloseRequest(accountRequestId, principal.getName());
    }

    @PutMapping("/api/account/close-request/reject/{accountRequestId}")
    public void rejectCloseRequest(@PathVariable Long accountRequestId,
                                   @Valid @RequestBody RejectRequestDto rejectRequestDto,
                                   Principal principal) {
        accountRequestService.rejectCloseRequest(accountRequestId, rejectRequestDto, principal.getName());
    }
}
