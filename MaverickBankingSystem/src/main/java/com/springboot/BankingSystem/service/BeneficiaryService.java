package com.springboot.BankingSystem.service;

import com.springboot.BankingSystem.dto.request.BeneficiaryDto;
import com.springboot.BankingSystem.dto.request.BeneficiaryTransactionDto;
import com.springboot.BankingSystem.dto.request.TransactionDto;
import com.springboot.BankingSystem.dto.response.BeneficiaryInfoDto;
import com.springboot.BankingSystem.exception.DuplicateRequestException;
import com.springboot.BankingSystem.exception.OwnershipException;
import com.springboot.BankingSystem.exception.ResourceNotFoundException;
import com.springboot.BankingSystem.model.Account;
import com.springboot.BankingSystem.model.Beneficiaries;
import com.springboot.BankingSystem.model.Customer;
import com.springboot.BankingSystem.repository.AccountRepository;
import com.springboot.BankingSystem.repository.BeneficiaryRepository;
import com.springboot.BankingSystem.repository.CustomerRepository;
import com.springboot.BankingSystem.utility.RoleCheckUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BeneficiaryService {
    private final RoleCheckUtility roleCheckUtility;
    private final BeneficiaryRepository beneficiaryRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionService transactionService;

    public void add(BeneficiaryDto beneficiaryDto, String loggedUsername) {
        // 1. check the user is CUSTOMER
        roleCheckUtility.checkCustomer(loggedUsername);

        // 2. check the account exists and active
        Account account = accountRepository.findByAccountNumber(beneficiaryDto.accountNumber());
        if(account == null)
            throw new ResourceNotFoundException("Account not found");

        // 3. get customer obj and verify the duplicate beneficiary
        Customer customer = customerRepository.findByUsername(loggedUsername);
        if(beneficiaryRepository.isExists(customer.getId(), beneficiaryDto.accountNumber()))
            throw new DuplicateRequestException("Beneficiary already exists");

        // 4. create beneficiary obj and save in db
        Beneficiaries beneficiary = new Beneficiaries();
        beneficiary.setName(beneficiaryDto.name());
        beneficiary.setAccountNumber(beneficiaryDto.accountNumber());
        beneficiary.setCustomer(customer);

        beneficiaryRepository.save(beneficiary);
    }

    public void delete(Long beneficiaryId, String loggedUsername) {
        // 1. check the user is CUSTOMER
        roleCheckUtility.checkCustomer(loggedUsername);

        // 2. get the beneficiary and check the owner and logged user are same
        Beneficiaries beneficiary = beneficiaryRepository.findById(beneficiaryId)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found"));

        if(!beneficiary.getCustomer().getUser().getUsername().equals(loggedUsername))
            throw new OwnershipException("Unauthorized User");

        // 3. remove from db
        beneficiaryRepository.delete(beneficiary);
    }

    public List<BeneficiaryInfoDto> get(String loggedUsername) {
        // 1. check the user is CUSTOMER
        roleCheckUtility.checkCustomer(loggedUsername);

        // 2. get the beneficiaries of the logged-in customer
        return beneficiaryRepository.getByUsername(loggedUsername);
    }

    public void transfer(Long accountId, Long beneficiaryId, BeneficiaryTransactionDto beneficiaryTransactionDto, String loggedUsername) {
        // 1. check the user is CUSTOMER
        roleCheckUtility.checkCustomer(loggedUsername);

        // 2. get the beneficiary and check the owner and logged user are same
        Beneficiaries beneficiary = beneficiaryRepository.findById(beneficiaryId)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found"));

        if(!beneficiary.getCustomer().getUser().getUsername().equals(loggedUsername))
            throw new OwnershipException("Unauthorized User");

        // 3. get the account and check the account holder and logged user are same
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if(!account.getCustomer().getUser().getUsername().equals(loggedUsername))
            throw new OwnershipException("Unauthorized User");

        // 4. fill the beneficiary account number and add the transaction
        TransactionDto transactionDto = new TransactionDto(
                beneficiaryTransactionDto.amount(),
                beneficiary.getAccountNumber(),
                beneficiaryTransactionDto.remarks()
        );

        transactionService.add(accountId, transactionDto, loggedUsername);
    }
}