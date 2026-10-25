package com.springboot.BankingSystem.repository;

import com.springboot.BankingSystem.dto.response.AccountInfoDto;
import com.springboot.BankingSystem.enums.AccountType;
import com.springboot.BankingSystem.model.Account;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account,Long> {

    @Query("""
select count(a.id) > 0
from Account a
where a.accountNumber = ?1
AND a.status != AccountStatus.CLOSED
""")
    boolean existsByAccountNumber(long accountNumber);

    @Query("""
select c.name, ac.accountNumber, a.accountType, ac.balance, b.ifscCode, ac.status, ac.openedOn, e.name, e.email
from AccountRequest a
join a.account ac
join a.customer c
join a.executive e
join e.branch b
""")
    List<AccountInfoDto> getAll(Pageable pageable);

    @Query("""
select c.name, ac.accountNumber, a.accountType, ac.balance, b.ifscCode, ac.status, ac.openedOn, e.name, e.email
from AccountRequest a
join a.customer c
join a.account ac
join a.executive e
join e.branch b
where c.id = ?1
""")
    List<AccountInfoDto> getByCustomer(Long customerId, Pageable pageable);

    @Query("""
select a
from Account a
where a.status = AccountStatus.ACTIVE
AND a.accountNumber = ?1
""")
    Account findByAccountNumber(Long accountNumber);

    @Query("""
select count(a.id) > 0
from Account a
join a.customer c
where c.id = ?1
AND a.status != AccountStatus.CLOSED
""")
    boolean getAccountByCustomerId(Long id);
}
