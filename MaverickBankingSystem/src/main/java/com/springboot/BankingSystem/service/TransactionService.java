package com.springboot.BankingSystem.service;

import com.springboot.BankingSystem.dto.request.TransactionDto;
import com.springboot.BankingSystem.dto.response.TransactionInfoDto;
import com.springboot.BankingSystem.enums.AccountStatus;
import com.springboot.BankingSystem.enums.TransactionStatus;
import com.springboot.BankingSystem.enums.TransactionType;
import com.springboot.BankingSystem.exception.InsufficientBalanceException;
import com.springboot.BankingSystem.exception.InvalidAccountStateException;
import com.springboot.BankingSystem.exception.OwnershipException;
import com.springboot.BankingSystem.exception.ResourceNotFoundException;
import com.springboot.BankingSystem.model.Account;
import com.springboot.BankingSystem.model.Transaction;
import com.springboot.BankingSystem.repository.AccountRepository;
import com.springboot.BankingSystem.repository.TransactionRepository;
import com.springboot.BankingSystem.utility.RoleCheckUtility;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final RoleCheckUtility roleCheckUtility;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public void add(Long accountId, @Valid TransactionDto transactionDto, String loggedUsername) {
        // 1. check the customer role
        roleCheckUtility.checkCustomer(loggedUsername);

        // 2. amount should be positive
        if(transactionDto.amount().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be greater than zero");

        // 3. check both account availability and status
        Account sender = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        Account receiver = accountRepository.findByAccountNumber(transactionDto.destinationAccount());
        if(receiver == null)
            throw new ResourceNotFoundException("Destination Account not found");

        if (sender.getStatus() != AccountStatus.ACTIVE)
            throw new InvalidAccountStateException("Account is not Active");

        if(sender.getId().equals(receiver.getId()))
            throw new InvalidAccountStateException("Source and Destination accounts are same");

        // 4. save a failed transaction if receiver's acc. is not active
        String referenceId = generateReferenceNumber();

        if(receiver.getStatus() != AccountStatus.ACTIVE){
            Transaction transaction = new Transaction();
            transaction.setReferenceNumber(referenceId);
            transaction.setTransactionType(TransactionType.DEBIT);
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transaction.setAmount(transactionDto.amount());
            transaction.setDestinationAccount(transactionDto.destinationAccount());
            transaction.setBalance(sender.getBalance());
            transaction.setRemarks("Receiver account is unavailable");
            transaction.setAccount(sender);
            transactionRepository.save(transaction);

            throw new InvalidAccountStateException("Receiver account is unavailable");
        }

        // 5. check the balance is sufficient and save a failed transaction
        if(sender.getBalance().compareTo(transactionDto.amount()) < 0) {
            Transaction transaction = new Transaction();
            transaction.setReferenceNumber(referenceId);
            transaction.setTransactionType(TransactionType.DEBIT);
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transaction.setAmount(transactionDto.amount());
            transaction.setDestinationAccount(transactionDto.destinationAccount());
            transaction.setBalance(sender.getBalance());
            transaction.setRemarks("Receiver account is unavailable");
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
        transaction.setDestinationAccount(sender.getAccountNumber());
        transaction.setBalance(receiver.getBalance());
        transaction.setRemarks(transactionDto.remarks());
        transaction.setAccount(receiver);

        transactionRepository.save(transaction);
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
        // 1. check the user is CUSTOMER
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

    public List<TransactionInfoDto> dynamicTransactionFilter(Long accountNumber, TransactionType transactionType, TransactionStatus transactionStatus, LocalDate from, LocalDate to, int page, int size, String loggedUsername) {
        // 1. check the logged user belongs to admin or executives
        roleCheckUtility.checkAdminOrExecutive(loggedUsername);

        // 2. set page and size
        Pageable pageable = PageRequest.of(page, size);

        // 3. get the list from repo and return
        return transactionRepository.dynamicTransactionFilter(accountNumber, transactionType, transactionStatus, from, to, pageable);
    }

    String generateReferenceNumber() {
        return "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
