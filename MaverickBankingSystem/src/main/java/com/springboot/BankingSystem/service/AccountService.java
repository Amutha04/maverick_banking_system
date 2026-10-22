package com.springboot.BankingSystem.service;

import com.springboot.BankingSystem.dto.response.AccountInfoDto;
import com.springboot.BankingSystem.dto.response.AccountRespDto;
import com.springboot.BankingSystem.enums.AccountStatus;
import com.springboot.BankingSystem.exception.*;
import com.springboot.BankingSystem.mapper.AccountMapper;
import com.springboot.BankingSystem.model.Account;
import com.springboot.BankingSystem.repository.AccountRepository;
import com.springboot.BankingSystem.repository.CustomerRepository;

import com.springboot.BankingSystem.utility.RoleCheckUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final RoleCheckUtility roleCheckUtility;

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
    }
}
