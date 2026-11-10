package com.springboot.BankingSystem.service;

import com.springboot.BankingSystem.dto.request.BalanceDto;
import com.springboot.BankingSystem.dto.request.DepositDto;
import com.springboot.BankingSystem.dto.request.PasswordDto;
import com.springboot.BankingSystem.dto.request.WithdrawDto;
import com.springboot.BankingSystem.dto.response.AccountInfoDto;
import com.springboot.BankingSystem.dto.response.AccountRespDto;
import com.springboot.BankingSystem.dto.response.PageResponseDto;
import com.springboot.BankingSystem.dto.response.BalanceInfoDto;
import com.springboot.BankingSystem.enums.AccountStatus;
import com.springboot.BankingSystem.enums.AccountType;
import com.springboot.BankingSystem.enums.BranchName;
import com.springboot.BankingSystem.enums.TransactionStatus;
import com.springboot.BankingSystem.enums.TransactionType;
import com.springboot.BankingSystem.exception.*;
import com.springboot.BankingSystem.mapper.AccountMapper;
import com.springboot.BankingSystem.mapper.PageMapper;
import com.springboot.BankingSystem.model.Account;
import com.springboot.BankingSystem.model.Branch;
import com.springboot.BankingSystem.model.Executive;
import com.springboot.BankingSystem.model.Transaction;
import com.springboot.BankingSystem.repository.AccountRepository;
import com.springboot.BankingSystem.repository.BranchRepository;
import com.springboot.BankingSystem.repository.CustomerRepository;
import com.springboot.BankingSystem.repository.ExecutiveRepository;

import com.springboot.BankingSystem.repository.TransactionRepository;
import com.springboot.BankingSystem.utility.RoleCheckUtility;
import com.springboot.BankingSystem.dto.response.StatDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final RoleCheckUtility roleCheckUtility;
    private final PasswordEncoder passwordEncoder;
    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;
    private final ExecutiveRepository executiveRepository;
    private final BranchRepository branchRepository;

    public AccountRespDto get(Long accountId, String loggedUsername) {
        // 1. get account by id
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        // 2. check the account holder and logged user are same
        if(! account.getCustomer().getUser().getUsername().equals(loggedUsername))
            throw new OwnershipException("Unauthorized User");

        // 3. map and return the account info
        return AccountMapper.convertAccountToDto(account);
    }

    public List<AccountInfoDto> getAll(int page, int size, String loggedUsername) {
        // 1. check the logged user belongs to admin or executives
        roleCheckUtility.checkAdminOrExecutive(loggedUsername);

        // 2. set page and size
        Pageable pageable = PageRequest.of(page, size);

        //3. get all accounts
        return accountRepository.getAll(pageable);
    }

    public List<AccountInfoDto> getByCustomer(int page, int size, Long customerId, String loggedUsername) {
        // 1. check the logged user belongs to admin or executives
        roleCheckUtility.checkAdminOrExecutive(loggedUsername);

        // 2. check the customer exists or not
        customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        // 3. set page and size
        Pageable pageable = PageRequest.of(page, size);

        //3. get accounts by customer
        return accountRepository.getByCustomer(customerId, pageable);
    }

    public void suspend(Long accountId, String loggedUsername) {
        // 1. check the logged user belongs to admin
        roleCheckUtility.checkAdmin(loggedUsername);

        // 2. get account by id
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        // 3. check the account status
        if(!account.getStatus().equals(AccountStatus.ACTIVE))
            throw new InvalidAccountStateException("Account is not Active");

        // 4. update the status
        account.setStatus(AccountStatus.SUSPENDED);

        // 5. save in db
        accountRepository.save(account);
        log.info("Account is suspended");
    }

    public void activate(Long accountId, String loggedUsername) {
        // 1. check the logged user belongs to admin
        roleCheckUtility.checkAdmin(loggedUsername);

        // 2. get account by id
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        // 3. only a suspended account can be activated again
        if(!account.getStatus().equals(AccountStatus.SUSPENDED))
            throw new InvalidAccountStateException("Account is not Suspended");

        // 4. update the status
        account.setStatus(AccountStatus.ACTIVE);

        // 5. save in db
        accountRepository.save(account);
        log.info("Account is activated");
    }

    public void setPassword(Long accountId, @Valid PasswordDto passwordDto, String loggedUsername) {
        // 1. check the logged user belongs to customer
        roleCheckUtility.checkCustomer(loggedUsername);

        // 2. get account by id and verify the customer
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if(!account.getCustomer().getUser().getUsername().equals(loggedUsername))
            throw new OwnershipException("Unauthorized User");

        // 3. check the account status
        if(!account.getStatus().equals(AccountStatus.ACTIVE))
            throw new InvalidAccountStateException("Account is not Active");

        // 4. set the password
        account.setPassword(passwordEncoder.encode(passwordDto.password()));

        // 5. save the changes
        accountRepository.save(account);
        log.info("Password changed");
    }

    @Transactional(noRollbackFor = InsufficientBalanceException.class)
    public void withdraw(Long accountId, @Valid WithdrawDto withdrawDto, String loggedUsername) {
        // 1. check the logged user belongs to customer
        roleCheckUtility.checkCustomer(loggedUsername);

        // 2. amount should be positive
        if(withdrawDto.amount().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be greater than zero");

        // 3. get account by id
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if(!account.getCustomer().getUser().getUsername().equals(loggedUsername))
            throw new OwnershipException("Unauthorized User");

        // 4. check the account status and password
        AccountStatus status = account.getStatus();
        if(!(status == AccountStatus.ACTIVE || status == AccountStatus.CLOSURE_REQUEST))
            throw new InvalidAccountStateException("Account is not Active");

        if(!passwordEncoder.matches(withdrawDto.password(), account.getPassword()))
            throw new PasswordMismatchException("Incorrect password");

        // 5. check the balance is sufficient and save a failed transaction
        String referenceId = transactionService.generateReferenceNumber();
        if(account.getBalance().compareTo(withdrawDto.amount()) < 0) {
            Transaction transaction = new Transaction();
            transaction.setReferenceNumber(referenceId);
            transaction.setSourceAccount(account.getAccountNumber());
            transaction.setTransactionType(TransactionType.WITHDRAW);
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transaction.setAmount(withdrawDto.amount());
            transaction.setBalance(account.getBalance());
            transaction.setRemarks("Insufficient balance");
            transaction.setAccount(account);

            transactionRepository.save(transaction);

            throw new InsufficientBalanceException("Insufficient balance");
        }

        // 6. update balance
        account.setBalance(account.getBalance().subtract(withdrawDto.amount()));
        accountRepository.save(account);

        // 7. save the transaction
        Transaction transaction = new Transaction();
        transaction.setReferenceNumber(referenceId);
        transaction.setSourceAccount(account.getAccountNumber());
        transaction.setTransactionType(TransactionType.WITHDRAW);
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);
        transaction.setAmount(withdrawDto.amount());
        transaction.setBalance(account.getBalance());
        transaction.setRemarks(withdrawDto.remarks());
        transaction.setAccount(account);

        transactionRepository.save(transaction);
        log.info("Transaction completed successfully");
    }

    @Transactional
    public void deposit(Long accountId, @Valid DepositDto depositDto, String loggedUsername) {
        // 1. check the logged user belongs to admin
        roleCheckUtility.checkCustomer(loggedUsername);

        // 2. amount should be positive
        if(depositDto.amount().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be greater than zero");

        // 3. get account by id
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if(!account.getCustomer().getUser().getUsername().equals(loggedUsername))
            throw new OwnershipException("Unauthorized User");

        // 4. check the account status
        if(!account.getStatus().equals(AccountStatus.ACTIVE))
            throw new InvalidAccountStateException("Account is not Active");

        // 5. update balance
        account.setBalance(account.getBalance().add(depositDto.amount()));
        accountRepository.save(account);

        // 6. save transaction
        Transaction transaction = new Transaction();
        transaction.setReferenceNumber(transactionService.generateReferenceNumber());
        transaction.setDestinationAccount(account.getAccountNumber());
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);
        transaction.setAmount(depositDto.amount());
        transaction.setBalance(account.getBalance());
        transaction.setRemarks(depositDto.remarks());
        transaction.setAccount(account);

        transactionRepository.save(transaction);
        log.info("Transaction completed successfully");
    }

    public List<AccountInfoDto> getByUsername(String loggedUsername) {
        // 1. check the logged user belongs to customer
        roleCheckUtility.checkCustomer(loggedUsername);

        // 2. get the accounts by username
        return accountRepository.getByUsername(loggedUsername);
    }

    public BalanceInfoDto checkBalance(@Valid BalanceDto balanceDto, String loggedUsername) {
        // 1. check the logged user belongs to customer
        roleCheckUtility.checkCustomer(loggedUsername);

        // 2. get account by id and
        Account account = accountRepository.findById(balanceDto.id())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if(!account.getCustomer().getUser().getUsername().equals(loggedUsername))
            throw new OwnershipException("Unauthorized User");

        // 2. check the password
        if(!passwordEncoder.matches(balanceDto.password(), account.getPassword()))
            throw new PasswordMismatchException("Incorrect password");

        // 3. return the balance
        return new BalanceInfoDto(account.getBalance());
    }

    public PageResponseDto<AccountInfoDto> dynamicAccountFilter(BranchName branchName, Long accountNumber, AccountType accountType, AccountStatus status, String customerName, int page, int size, String loggedUsername) {
        // 1. check the logged user belongs to admin or executives
        roleCheckUtility.checkAdminOrExecutive(loggedUsername);

        // 2. executive sees only the own branch, admin sees the branch chosen in the filter or all
        Executive executive = executiveRepository.findByUsername(loggedUsername);
        Long branchId = null;
        if(executive != null)
            branchId = executive.getBranch().getId();
        else if(branchName != null) {
            Branch branch = branchRepository.findByName(branchName);
            if(branch == null)
                throw new ResourceNotFoundException("Branch not found");
            branchId = branch.getId();
        }

        // 3. set page and size
        Pageable pageable = PageRequest.of(page, size);

        // 4. get the list with the pagination info
        return PageMapper.convertPageToDto(accountRepository.dynamicAccountFilter(branchId, accountNumber, accountType, status, customerName, pageable));
    }

    public StatDto getAccountsByType(String loggedUsername) {
        // 1. check the logged user belongs to admin or executives
        roleCheckUtility.checkAdminOrExecutive(loggedUsername);

        // 2. executive sees only the own branch, admin sees all
        Executive executive = executiveRepository.findByUsername(loggedUsername);
        Long branchId = executive == null ? null : executive.getBranch().getId();

        // 3. count the open accounts for each type, the dashboard chart shows them
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        for(AccountType type : AccountType.values()) {
            labels.add(type.toString());
            values.add(accountRepository.getAccountCountByType(branchId, type));
        }

        return new StatDto(labels, values);
    }

    public StatDto getAccountsByBranch(String loggedUsername) {
        // 1. check the logged user belongs to admin, this one is bank-wide
        roleCheckUtility.checkAdmin(loggedUsername);

        // 2. count the open accounts
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        for(Branch branch : branchRepository.findAll()) {
            labels.add(branch.getName().toString());
            values.add(accountRepository.getAccountCountByBranch(branch.getId()));
        }

        return new StatDto(labels, values);
    }
}
