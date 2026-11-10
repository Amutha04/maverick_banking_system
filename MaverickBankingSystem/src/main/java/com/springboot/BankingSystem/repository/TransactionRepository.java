package com.springboot.BankingSystem.repository;

import com.springboot.BankingSystem.dto.response.TransactionInfoDto;
import com.springboot.BankingSystem.enums.TransactionStatus;
import com.springboot.BankingSystem.enums.TransactionType;
import com.springboot.BankingSystem.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("""
select t.referenceNumber, t.transactionType, t.transactionStatus, t.amount, t.sourceAccount, t.destinationAccount, t.balance, t.timeStamp, t.remarks
from Transaction t
""")
    List<TransactionInfoDto> getAll(Pageable pageable);

    @Query("""
select t.referenceNumber, t.transactionType, t.transactionStatus, t.amount, t.sourceAccount, t.destinationAccount, t.balance, t.timeStamp, t.remarks
from Transaction t
join t.account a
where a.id = ?1 AND (a.status = AccountStatus.ACTIVE OR a.status = AccountStatus.CLOSURE_REQUEST)
""")
    List<TransactionInfoDto> getByAccount(Long accountId, Pageable pageable);

    @Query(value = """
select t.referenceNumber, t.transactionType, t.transactionStatus, t.amount, t.sourceAccount, t.destinationAccount, t.balance, t.timeStamp, t.remarks
from Transaction t
join t.account a
where (?1 is null OR a.branch.id = ?1)
AND (?2 is null OR a.accountNumber = ?2)
AND (?3 is null OR t.transactionType = ?3)
AND (?4 is null OR t.transactionStatus = ?4)
AND (?5 is null OR t.timeStamp >= ?5)
AND (?6 is null OR t.timeStamp <= ?6)
""")
    Page<TransactionInfoDto> dynamicTransactionFilter(Long branchId, Long accountNumber, TransactionType transactionType, TransactionStatus transactionStatus, LocalDate from, LocalDate to, Pageable pageable);

    @Query(value = """
select t.referenceNumber, t.transactionType, t.transactionStatus, t.amount, t.sourceAccount, t.destinationAccount, t.balance, t.timeStamp, t.remarks
from Transaction t
join t.account a
join a.customer c
join c.user u
where u.username = ?1 AND (a.status = AccountStatus.ACTIVE OR a.status = AccountStatus.CLOSURE_REQUEST)
AND (?2 is null OR a.accountNumber = ?2)
AND (?3 is null OR t.transactionType = ?3)
AND (?4 is null OR t.transactionStatus = ?4)
AND (?5 is null OR t.timeStamp >= ?5)
AND (?6 is null OR t.timeStamp <= ?6)
""")
    Page<TransactionInfoDto> getByUsername(String loggedUsername, Long accountNumber, TransactionType transactionType, TransactionStatus transactionStatus, LocalDate from, LocalDate to, Pageable pageable);
}
