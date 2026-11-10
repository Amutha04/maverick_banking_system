package com.springboot.BankingSystem.repository;

import com.springboot.BankingSystem.dto.response.AccountInfoDto;
import com.springboot.BankingSystem.enums.AccountStatus;
import com.springboot.BankingSystem.enums.AccountType;
import com.springboot.BankingSystem.model.Account;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
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
""")
    boolean existsByAccountNumber(long accountNumber);

    @Query("""
select ac.id, c.name, ac.accountNumber, ac.accountType, ac.balance, b.ifscCode, b.name, b.district, ac.status, ac.openedOn, e.name, e.email
from Account ac
join ac.customer c
join ac.branch b
left join AccountRequest a on a.account = ac and a.requestType = RequestType.OPEN
left join a.executive e
""")
    List<AccountInfoDto> getAll(Pageable pageable);

    @Query("""
select ac.id, c.name, ac.accountNumber, ac.accountType, ac.balance, b.ifscCode, b.name, b.district, ac.status, ac.openedOn, e.name, e.email
from Account ac
join ac.customer c
join ac.branch b
left join AccountRequest a on a.account = ac and a.requestType = RequestType.OPEN
left join a.executive e
where c.id = ?1
""")
    List<AccountInfoDto> getByCustomer(Long customerId, Pageable pageable);

    @Query("""
select a
from Account a
where a.accountNumber = ?1
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

    @Query("""
select ac.id, c.name, ac.accountNumber, ac.accountType, ac.balance, b.ifscCode, b.name, b.district, ac.status, ac.openedOn, e.name, e.email
from Account ac
join ac.customer c
join ac.branch b
left join AccountRequest a on a.account = ac and a.requestType = RequestType.OPEN
left join a.executive e
join c.user u
where u.username = ?1 AND u.isActive = true
""")
    List<AccountInfoDto> getByUsername(String loggedUsername);

    @Query(value = """
select ac.id, c.name, ac.accountNumber, ac.accountType, ac.balance, b.ifscCode, b.name, b.district, ac.status, ac.openedOn, e.name, e.email
from Account ac
join ac.customer c
join ac.branch b
left join AccountRequest a on a.account = ac and a.requestType = RequestType.OPEN
left join a.executive e
where (?1 is null OR b.id = ?1)
AND (?2 is null OR ac.accountNumber = ?2)
AND (?3 is null OR ac.accountType = ?3)
AND (?4 is null OR ac.status = ?4)
AND (?5 is null OR c.name = ?5)
""")
    Page<AccountInfoDto> dynamicAccountFilter(Long branchId, Long accountNumber, AccountType accountType, AccountStatus status, String customerName, Pageable pageable);

    @Query("""
select count(a.id)
from Account a
where (?1 is null OR a.branch.id = ?1)
AND a.accountType = ?2
AND a.status != AccountStatus.CLOSED
""")
    long getAccountCountByType(Long branchId, AccountType accountType);

    @Query("""
select count(a.id)
from Account a
where a.branch.id = ?1
AND a.status != AccountStatus.CLOSED
""")
    long getAccountCountByBranch(Long branchId);
}
