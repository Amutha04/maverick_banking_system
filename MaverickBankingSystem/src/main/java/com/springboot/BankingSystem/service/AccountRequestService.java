package com.springboot.BankingSystem.service;

import com.springboot.BankingSystem.dto.request.AccountDto;
import com.springboot.BankingSystem.dto.request.RejectRequestDto;
import com.springboot.BankingSystem.enums.*;
import com.springboot.BankingSystem.exception.*;
import com.springboot.BankingSystem.model.*;
import com.springboot.BankingSystem.repository.*;
import com.springboot.BankingSystem.utility.RoleCheckUtility;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AccountRequestService {

    private final CustomerRepository customerRepository;
    private final AccountRequestRepository accountRequestRepository;
    private final ExecutiveRepository executiveRepository;
    private final AccountRepository accountRepository;
    private final LoanRepository loanRepository;
    private final RoleCheckUtility roleCheckUtility;

    public void request(AccountDto accountDto, String loggedUsername) {
        // 1. check the user is customer
        roleCheckUtility.checkCustomer(loggedUsername);

        // 2. get customer obj and verify the duplicate request
        Customer customer = customerRepository.findByUsername(loggedUsername);
        boolean isDuplicated = accountRequestRepository.isExists(customer.getId(), accountDto.requestType());
        if(isDuplicated)
            throw new DuplicateRequestException("Request already exists");

        // 3. create accountRequest obj
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setRequestType(accountDto.requestType());
        accountRequest.setAccountType(accountDto.accountType());
        if(accountDto.requestType() == RequestType.OPEN)
            accountRequest.setInitialDeposit(accountDto.initialDeposit());
        else
            accountRequest.setReason(accountDto.reason());
        accountRequest.setRequestedOn(LocalDate.now());

        // 4. inject customer obj into req. obj
        accountRequest.setCustomer(customer);

        // 5. save request in db
        accountRequestRepository.save(accountRequest);
    }

    public void approveOpenRequest(Long accountRequestId, String loggedUsername) {
        // 1. check the user is executive
        roleCheckUtility.checkExecutive(loggedUsername);

        // 2. get the request and Verify the status
        AccountRequest accountRequest = accountRequestRepository.findById(accountRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if(!accountRequest.getCustomer().getUser().isActive())
            throw new ResourceNotFoundException("Customer not found");

        if(accountRequest.getRequestStatus() != RequestStatus.IN_PROCESS)
            throw new InvalidAccountStateException("Request is already " + accountRequest.getRequestStatus());

        // 4. Verify the KYC status
        if(accountRequest.getCustomer().getKycStatus() == KYCStatus.PENDING)
            throw new IncompleteVerificationException("Incomplete KYC");

        // 5. Verify the age is above 18
        LocalDate dob = accountRequest.getCustomer().getDob();
        if(dob.isAfter(LocalDate.now().minusYears(18)))
            throw new EligibilityException("Age must be 18 or above");

        // 6. create the account and save in db
        Long accountNumber = generateAccountNumber();
        Executive executive = executiveRepository.findByUsername(loggedUsername);

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setAccountType(accountRequest.getAccountType());
        account.setBalance(accountRequest.getInitialDeposit());
        account.setOpenedOn(LocalDate.now());
        account.setStatus(AccountStatus.ACTIVE);
        account.setCustomer(accountRequest.getCustomer());
        account.setBranch(executive.getBranch());

        accountRepository.save(account);

        // 7. approve the request and update in db
        accountRequest.setRequestStatus(RequestStatus.APPROVED);
        accountRequest.setProcessedOn(LocalDate.now());
        accountRequest.setExecutive(executive);
        accountRequest.setAccount(account);

        accountRequestRepository.save(accountRequest);
    }

    private Long generateAccountNumber() {
        long accountNumber;
        boolean unique;

        do {
            // to get 12 digit random number
            accountNumber = (long) (Math.random() * 900000000000L) + 100000000000L;
            // to check whether the generated number is already exist or not
            unique = accountRepository.existsByAccountNumber(accountNumber);

        } while (unique);

        return accountNumber;
    }

    public void rejectRequest(Long accountRequestId, @Valid RejectRequestDto rejectRequestDto, String loggedUsername) {
        // 1. check the user is executive
        roleCheckUtility.checkExecutive(loggedUsername);

        // 2. check the reason if null or not
        if (rejectRequestDto.reason() == null || rejectRequestDto.reason().isBlank())
            throw new IllegalArgumentException("Rejection reason is required");

        // 3. get the request and Verify the status
        AccountRequest accountRequest = accountRequestRepository.findById(accountRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if(!accountRequest.getCustomer().getUser().isActive())
            throw new ResourceNotFoundException("Customer not found");

        if(accountRequest.getRequestStatus() != RequestStatus.IN_PROCESS)
            throw new InvalidAccountStateException("Request is already " + accountRequest.getRequestStatus());

        // 4.update the status
        Executive executive = executiveRepository.findByUsername(loggedUsername);

        accountRequest.setRequestStatus(RequestStatus.REJECTED);
        accountRequest.setProcessedOn(LocalDate.now());
        accountRequest.setRemark(rejectRequestDto.reason());
        accountRequest.setExecutive(executive);

        accountRequestRepository.save(accountRequest);
    }

    public void approveCloseRequest(Long accountRequestId, String loggedUsername) {
        // 1. check the logged user belongs to executive
        roleCheckUtility.checkExecutive(loggedUsername);

        // 2. get the request and Verify the status
        AccountRequest accountRequest = accountRequestRepository.findById(accountRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if(!accountRequest.getCustomer().getUser().isActive())
            throw new ResourceNotFoundException("Customer not found");

        if(accountRequest.getRequestStatus() != RequestStatus.IN_PROCESS)
            throw new InvalidAccountStateException("Request is already " + accountRequest.getRequestStatus());

        // 3. get account by the request and check the status
        Account account = accountRequest.getAccount();

        if(!account.getStatus().equals(AccountStatus.ACTIVE)){
            throw new InvalidAccountStateException("Account is not Active");
        }

        // 4. check the balance is empty
        if(account.getBalance().compareTo(BigDecimal.ZERO) != 0)
            throw new InvalidAccountStateException("Withdraw your balance amount");

        // 5. check for any open loan
        if(loanRepository.checkForOpenLoan(account.getId()))
            throw new InvalidAccountStateException("All Loans must be closed");

        // 6. reject the loan which are in IN_PROCESS or SANCTIONED
        loanRepository.rejectNotOpenedLoans(account.getId(), "Account is closed");

        // 7. close the account and approve the request
        account.setStatus(AccountStatus.CLOSED);
        account.setClosed_on(LocalDate.now());
        accountRepository.save(account);

        accountRequest.setRequestStatus(RequestStatus.APPROVED);
        accountRequest.setProcessedOn(LocalDate.now());
        accountRequestRepository.save(accountRequest);
    }
}
