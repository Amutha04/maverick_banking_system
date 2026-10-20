package com.springboot.BankingSystem.mapper;

import com.springboot.BankingSystem.dto.response.AccountRespDto;
import com.springboot.BankingSystem.model.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {
    public static AccountRespDto convertAccountToDto(Account account) {
        return new AccountRespDto(
                account.getCustomer().getName(),
                account.getAccountNumber(),
                account.getBranch().getIfscCode(),
                account.getBranch().getName(),
                account.getBranch().getDistrict(),
                account.getBranch().getState()
        );
    }
}
