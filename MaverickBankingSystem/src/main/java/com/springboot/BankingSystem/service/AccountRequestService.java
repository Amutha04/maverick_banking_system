package com.springboot.BankingSystem.service;

import com.springboot.BankingSystem.dto.request.AccountDto;
import com.springboot.BankingSystem.dto.request.RejectRequestDto;
import com.springboot.BankingSystem.dto.response.AccountRequestInfoDto;
import com.springboot.BankingSystem.dto.response.PageResponseDto;
import com.springboot.BankingSystem.enums.*;
import com.springboot.BankingSystem.exception.*;
import com.springboot.BankingSystem.mapper.PageMapper;
import com.springboot.BankingSystem.model.*;
import com.springboot.BankingSystem.repository.*;
import com.springboot.BankingSystem.utility.RoleCheckUtility;
import com.springboot.BankingSystem.dto.response.StatDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountRequestService {

    private final CustomerRepository customerRepository;
    private final AccountRequestRepository accountRequestRepository;
    private final ExecutiveRepository executiveRepository;
    private final AccountRepository accountRepository;
    private final LoanRepository loanRepository;
    private final RoleCheckUtility roleCheckUtility;
    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;
    private final BranchRepository branchRepository;

    @Transactional
    public void request(AccountDto accountDto, Long accountId, String loggedUsername) throws IOException {
        // 1. check the user is customer
        roleCheckUtility.checkCustomer(loggedUsername);

        // 2. get customer obj
        Customer customer = customerRepository.findByUsername(loggedUsername);

        // 3. create accountRequest obj
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setRequestType(accountDto.requestType());
        accountRequest.setAccountType(accountDto.accountType());

        if(accountDto.requestType() == RequestType.OPEN) {

            boolean isDuplicated = accountRequestRepository.isExistsForOpen(customer.getId(), accountDto.accountType());
            if(isDuplicated)
                throw new DuplicateRequestException("Request already exists");

            // keep the existing aadhaar and pan, fill them only when the customer has not given them yet
            if(customer.getAadhaarNumber() == null)
                customer.setAadhaarNumber(accountDto.aadhaarNumber());

            if(customer.getPanNumber() == null)
                customer.setPanNumber(accountDto.panNumber());
            accountRequest.setInitialDeposit(accountDto.initialDeposit());
            accountRequest.setBranchName(accountDto.branchName());
        }
        else {

            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

            if(!account.getCustomer().getUser().getUsername().equals(loggedUsername))
                throw new OwnershipException("Unauthorized User");

            if(account.getStatus() != AccountStatus.ACTIVE)
                throw new InvalidAccountStateException("Account is not active");

            account.setStatus(AccountStatus.CLOSURE_REQUEST);
            accountRepository.save(account);

            accountRequest.setBranchName(account.getBranch().getName().toString());
            accountRequest.setReason(accountDto.reason());
            accountRequest.setAccount(account);
        }
        accountRequest.setRequestedOn(LocalDate.now());

        // 5. inject customer obj into req. obj
        accountRequest.setCustomer(customer);

        // 6. save request in db
        accountRequestRepository.save(accountRequest);
        log.info("Request is received");
    }

    @Transactional
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

        // 3. the request must be assigned to this executive
        Executive executive = executiveRepository.findByUsername(loggedUsername);
        checkAssignment(accountRequest, executive);

        // 4. Verify the KYC status
        if(accountRequest.getCustomer().getKycStatus() == KYCStatus.PENDING)
            throw new IncompleteVerificationException("Incomplete KYC");

        // 5. Verify the age is above 18
        LocalDate dob = accountRequest.getCustomer().getDob();
        if(dob.isAfter(LocalDate.now().minusYears(18)))
            throw new EligibilityException("Age must be 18 or above");

        // 6. create the account and save in db
        Long accountNumber = generateAccountNumber();

        BigDecimal initialDeposit = accountRequest.getInitialDeposit() == null ? BigDecimal.ZERO : accountRequest.getInitialDeposit();

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setAccountType(accountRequest.getAccountType());
        account.setBalance(initialDeposit);
        account.setOpenedOn(LocalDate.now());
        account.setStatus(AccountStatus.ACTIVE);
        account.setCustomer(accountRequest.getCustomer());
        account.setBranch(getRequestedBranch(accountRequest, executive));

        accountRepository.save(account);
        log.info("New Account is created");

        // 7. save the deposit transaction for the opening balance
        if(initialDeposit.compareTo(BigDecimal.ZERO) > 0) {
            Transaction transaction = new Transaction();
            transaction.setReferenceNumber(transactionService.generateReferenceNumber());
            transaction.setDestinationAccount(account.getAccountNumber());
            transaction.setTransactionType(TransactionType.DEPOSIT);
            transaction.setTransactionStatus(TransactionStatus.SUCCESS);
            transaction.setAmount(initialDeposit);
            transaction.setBalance(account.getBalance());
            transaction.setRemarks("Initial deposit");
            transaction.setAccount(account);

            transactionRepository.save(transaction);
            log.info("Transaction completed successfully");
        }

        // 8. approve the request and update in db
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

    @Transactional
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

        // 4. the request must be assigned to this executive
        Executive executive = executiveRepository.findByUsername(loggedUsername);
        checkAssignment(accountRequest, executive);

        // 5. for the close request, make the account active again
        Account account = accountRequest.getAccount();
        if(accountRequest.getRequestType() == RequestType.CLOSE && account != null && account.getStatus() == AccountStatus.CLOSURE_REQUEST) {
            account.setStatus(AccountStatus.ACTIVE);
            accountRepository.save(account);
        }

        // 6. update the status
        accountRequest.setRequestStatus(RequestStatus.REJECTED);
        accountRequest.setProcessedOn(LocalDate.now());
        accountRequest.setRemark(rejectRequestDto.reason());
        accountRequest.setExecutive(executive);

        accountRequestRepository.save(accountRequest);
        log.info("Request is rejected");
    }

    @Transactional
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

        Executive executive = executiveRepository.findByUsername(loggedUsername);
        checkAssignment(accountRequest, executive);

        // 3. get account by the request and check the status
        Account account = accountRequest.getAccount();

        if(account.getStatus() != AccountStatus.CLOSURE_REQUEST)
            throw new InvalidAccountStateException("Account is not in closure request");

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
        account.setClosedOn(LocalDate.now());
        accountRepository.save(account);

        accountRequest.setRequestStatus(RequestStatus.APPROVED);
        accountRequest.setProcessedOn(LocalDate.now());
        accountRequest.setExecutive(executive);
        accountRequestRepository.save(accountRequest);
        log.info("Request is approved");
    }

    public PageResponseDto<AccountRequestInfoDto> getByUsername(RequestType requestType, RequestStatus requestStatus, LocalDate requestedFrom, LocalDate requestedTo, int page, int size, String loggedUsername) {
        // 1. check the logged user belongs to customer
        roleCheckUtility.checkCustomer(loggedUsername);

        // 2. set page and size
        Pageable pageable = PageRequest.of(page, size);

        // 3. return the request list with the filters and the pagination info
        return PageMapper.convertPageToDto(accountRequestRepository.getByUsername(loggedUsername, requestType, requestStatus, requestedFrom, requestedTo, pageable));
    }

    public void assignExecutive(Long accountRequestId, Long executiveId, String loggedUsername) {
        // 1. check the logged user belongs to manager
        roleCheckUtility.checkManager(loggedUsername);

        // 2. get the request and Verify the status
        AccountRequest accountRequest = accountRequestRepository.findById(accountRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if(!accountRequest.getCustomer().getUser().isActive())
            throw new ResourceNotFoundException("Customer not found");

        if(accountRequest.getRequestStatus() != RequestStatus.IN_PROCESS)
            throw new InvalidAccountStateException("Request is already " + accountRequest.getRequestStatus());

        if(accountRequest.getExecutive() != null)
            throw new InvalidAccountStateException("Request is already assigned to " + accountRequest.getExecutive().getName());

        // 3. the request must belong to the manager's branch
        Executive manager = executiveRepository.findByUsername(loggedUsername);
        if(accountRequest.getBranchName() == null || !accountRequest.getBranchName().equals(manager.getBranch().getName().toString()))
            throw new OwnershipException("Request belongs to another branch");

        // 4. check the executive
        Executive executive = executiveRepository.findById(executiveId)
                .orElseThrow(() -> new ResourceNotFoundException("Executive not found"));

        if(!executive.getUser().isActive())
            throw new InActiveUserException("Executive is not active");

        // 5. check the branch
        if(!accountRequest.getBranchName().equals(executive.getBranch().getName().toString()))
            throw new IllegalArgumentException("Branch mismatched");

        // 6. save the executive
        accountRequest.setExecutive(executive);
        accountRequestRepository.save(accountRequest);
        log.info("Executive is assigned");
    }

    public PageResponseDto<AccountRequestInfoDto> dynamicAccountRequestFilter(BranchName branchName, RequestType requestType, RequestStatus requestStatus, String customerName, boolean unassigned, LocalDate requestedFrom, LocalDate requestedTo, int page, int size, String loggedUsername) {
        // 1. check the logged user belongs to admin or executives
        roleCheckUtility.checkAdminOrExecutive(loggedUsername);

        // 2. executive sees only the own branch, admin sees the branch chosen in the filter or all
        Executive executive = executiveRepository.findByUsername(loggedUsername);
        String branch = executive != null ? executive.getBranch().getName().toString() : (branchName == null ? null : branchName.toString());

        // 3. a plain executive sees only the requests assigned to them, the manager and the admin see the whole branch
        Long executiveId = (executive != null && executive.getJobTitle() != JobTitle.BANK_MANAGER) ? executive.getId() : null;

        // 4. set page and size
        Pageable pageable = PageRequest.of(page, size);

        // 5. get the list with the pagination info
        return PageMapper.convertPageToDto(accountRequestRepository.dynamicAccountRequestFilter(branch, executiveId, requestType, requestStatus, customerName, requestedFrom, requestedTo, unassigned, pageable));
    }

    private void checkAssignment(AccountRequest accountRequest, Executive executive) {
        // an assigned request is handled only by that executive, an unassigned one by any executive of the same branch
        if(accountRequest.getExecutive() != null) {
            if(!accountRequest.getExecutive().getId().equals(executive.getId()))
                throw new OwnershipException("Request is assigned to another executive");
        }
        else if(accountRequest.getBranchName() != null && !accountRequest.getBranchName().equals(executive.getBranch().getName().toString()))
            throw new OwnershipException("Request belongs to another branch");
    }

    private Branch getRequestedBranch(AccountRequest accountRequest, Executive executive) {
        // the account goes to the branch the customer asked for, the executive's branch is the fallback
        if(accountRequest.getBranchName() == null)
            return executive.getBranch();

        Branch branch = branchRepository.findByName(BranchName.valueOf(accountRequest.getBranchName()));
        return branch == null ? executive.getBranch() : branch;
    }

    public StatDto getAccountRequestsByStatus(String loggedUsername) {
        // 1. check the logged user belongs to admin or executives
        roleCheckUtility.checkAdminOrExecutive(loggedUsername);

        // 2. executive sees only the own branch, admin sees all
        Executive executive = executiveRepository.findByUsername(loggedUsername);
        String branchName = executive == null ? null : executive.getBranch().getName().toString();

        // 3. count the requests for each status, the dashboard chart shows them
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        for(RequestStatus status : RequestStatus.values()) {
            labels.add(status.toString());
            values.add(accountRequestRepository.getRequestCountByStatus(branchName, status));
        }

        return new StatDto(labels, values);
    }
}
