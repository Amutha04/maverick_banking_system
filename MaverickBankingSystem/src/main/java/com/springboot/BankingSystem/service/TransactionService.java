package com.springboot.BankingSystem.service;

import com.springboot.BankingSystem.dto.request.TransactionDto;
import com.springboot.BankingSystem.dto.response.PageResponseDto;
import com.springboot.BankingSystem.dto.response.TransactionInfoDto;
import com.springboot.BankingSystem.enums.AccountStatus;
import com.springboot.BankingSystem.enums.BranchName;
import com.springboot.BankingSystem.enums.TransactionStatus;
import com.springboot.BankingSystem.enums.TransactionType;
import com.springboot.BankingSystem.exception.*;
import com.springboot.BankingSystem.mapper.PageMapper;
import com.springboot.BankingSystem.model.Account;
import com.springboot.BankingSystem.model.Branch;
import com.springboot.BankingSystem.model.Executive;
import com.springboot.BankingSystem.model.Transaction;
import com.springboot.BankingSystem.repository.AccountRepository;
import com.springboot.BankingSystem.repository.BranchRepository;
import com.springboot.BankingSystem.repository.ExecutiveRepository;
import com.springboot.BankingSystem.repository.TransactionRepository;
import com.springboot.BankingSystem.utility.RoleCheckUtility;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {
    private final RoleCheckUtility roleCheckUtility;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;
    private final ExecutiveRepository executiveRepository;
    private final BranchRepository branchRepository;

    @Transactional(noRollbackFor = {InvalidAccountStateException.class, InsufficientBalanceException.class})
    public void add(Long accountId, @Valid TransactionDto transactionDto, String loggedUsername) {
        // 1. check the customer role
        roleCheckUtility.checkCustomer(loggedUsername);

        // 2. amount should be positive
        if(transactionDto.amount().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be greater than zero");

        // 3. check sender's account and password
        Account sender = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if(!sender.getCustomer().getUser().getUsername().equals(loggedUsername))
            throw new OwnershipException("Unauthorized User");

        if (sender.getStatus() != AccountStatus.ACTIVE)
            throw new InvalidAccountStateException("Account is not Active");

        if(!passwordEncoder.matches(transactionDto.password(), sender.getPassword()))
            throw new PasswordMismatchException("Incorrect password");

        // 4. check receiver's account and save a failed transaction
        Account receiver = accountRepository.findByAccountNumber(transactionDto.destinationAccount());
        String referenceId = generateReferenceNumber();

        if(receiver == null)
            throw new ResourceNotFoundException("Destination account is not found");

        if(receiver.getStatus() != AccountStatus.ACTIVE) {
            Transaction transaction = new Transaction();
            transaction.setReferenceNumber(referenceId);
            transaction.setTransactionType(TransactionType.DEBIT);
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transaction.setAmount(transactionDto.amount());
            transaction.setSourceAccount(sender.getAccountNumber());
            transaction.setDestinationAccount(transactionDto.destinationAccount());
            transaction.setBalance(sender.getBalance());
            transaction.setRemarks("Receiver account is unavailable");
            transaction.setAccount(sender);
            transactionRepository.save(transaction);

            throw new InvalidAccountStateException("Receiver account is unavailable");
        }

        if(sender.getId().equals(receiver.getId()))
            throw new InvalidAccountStateException("Source and Destination accounts are same");

        // 5. check the balance is sufficient and save a failed transaction
        if(sender.getBalance().compareTo(transactionDto.amount()) < 0) {
            Transaction transaction = new Transaction();
            transaction.setReferenceNumber(referenceId);
            transaction.setTransactionType(TransactionType.DEBIT);
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transaction.setAmount(transactionDto.amount());
            transaction.setSourceAccount(sender.getAccountNumber());
            transaction.setDestinationAccount(transactionDto.destinationAccount());
            transaction.setBalance(sender.getBalance());
            transaction.setRemarks("Insufficient balance");
            transaction.setAccount(sender);
            transactionRepository.save(transaction);

            throw new InsufficientBalanceException("Insufficient balance");
        }

        // 6. reduce the amount from sender, add to receiver and update
        sender.setBalance(sender.getBalance().subtract(transactionDto.amount()));
        receiver.setBalance(receiver.getBalance().add(transactionDto.amount()));
        accountRepository.save(sender);
        accountRepository.save(receiver);

        // 7. create and save the transaction on both accounts
        Transaction transaction = new Transaction();

        transaction.setReferenceNumber(referenceId);
        transaction.setTransactionType(TransactionType.DEBIT);
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);
        transaction.setAmount(transactionDto.amount());
        transaction.setSourceAccount(sender.getAccountNumber());
        transaction.setDestinationAccount(transactionDto.destinationAccount());
        transaction.setBalance(sender.getBalance());
        transaction.setRemarks(transactionDto.remarks());
        transaction.setAccount(sender);

        transactionRepository.save(transaction);

        transaction = new Transaction();
        transaction.setReferenceNumber(referenceId);
        transaction.setTransactionType(TransactionType.CREDIT);
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);
        transaction.setAmount(transactionDto.amount());
        transaction.setSourceAccount(sender.getAccountNumber());
        transaction.setDestinationAccount(receiver.getAccountNumber());
        transaction.setBalance(receiver.getBalance());
        transaction.setRemarks(transactionDto.remarks());
        transaction.setAccount(receiver);

        transactionRepository.save(transaction);
        log.info("Transaction completed successfully");
    }

    public List<TransactionInfoDto> getAll(int page, int size, String loggedUsername) {
        // 1. check the logged user belongs to admin or executives
        roleCheckUtility.checkAdminOrExecutive(loggedUsername);

        // 2. set page and size
        Pageable pageable = PageRequest.of(page, size);

        // 3. get all transactions
        return transactionRepository.getAll(pageable);
    }

    public List<TransactionInfoDto> getByAccount(int page, int size, Long accountId, String loggedUsername) {
        // 1. check the logged user belongs to admin or executives
        roleCheckUtility.checkAdminOrExecutive(loggedUsername);

        // 2. check the account exists or not
        accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        // 3. set page and size
        Pageable pageable = PageRequest.of(page, size);

        // 4. get transactions by account
        return transactionRepository.getByAccount(accountId, pageable);
    }

    public List<TransactionInfoDto> get(int page, int size, Long accountId, String loggedUsername) {
        // 1. check the user is customer
        roleCheckUtility.checkCustomer(loggedUsername);

        // 2. get the account and check the account holder and logged user are same
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if(!account.getCustomer().getUser().getUsername().equals(loggedUsername))
            throw new OwnershipException("Unauthorized User");

        // 3. set page and size
        Pageable pageable = PageRequest.of(page, size);

        // 4. get transactions of the account
        return transactionRepository.getByAccount(accountId, pageable);
    }

    public PageResponseDto<TransactionInfoDto> dynamicTransactionFilter(BranchName branchName, Long accountNumber, TransactionType transactionType, TransactionStatus transactionStatus, LocalDate from, LocalDate to, int page, int size, String loggedUsername) {
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
        return PageMapper.convertPageToDto(transactionRepository.dynamicTransactionFilter(branchId, accountNumber, transactionType, transactionStatus, from, to, pageable));
    }

    String generateReferenceNumber() {
        return "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    public PageResponseDto<TransactionInfoDto> getByUsername(Long accountNumber, TransactionType transactionType, TransactionStatus transactionStatus, LocalDate from, LocalDate to, int page, int size, String loggedUsername) {
        // 1. check the user is customer
        roleCheckUtility.checkCustomer(loggedUsername);

        // 2. set page and size
        Pageable pageable = PageRequest.of(page, size);

        // 3. get the transactions of the customer with the filters and the pagination info
        return PageMapper.convertPageToDto(transactionRepository.getByUsername(loggedUsername, accountNumber, transactionType, transactionStatus, from, to, pageable));
    }
}
