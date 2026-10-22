package com.springboot.BankingSystem.repository;

import com.springboot.BankingSystem.dto.response.TransactionInfoDto;
import com.springboot.BankingSystem.enums.TransactionStatus;
import com.springboot.BankingSystem.enums.TransactionType;
import com.springboot.BankingSystem.model.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("""
select t.referenceNumber, t.transactionType, t.transactionStatus, t.amount, a.accountNumber, t.destinationAccount, t.balance, t.timeStamp, t.remarks
from Transaction t
join t.account a
order by t.id desc
""")
    List<TransactionInfoDto> getAll(Pageable pageable);

    @Query("""
select t.referenceNumber, t.transactionType, t.transactionStatus, t.amount, a.accountNumber, t.destinationAccount, t.balance, t.timeStamp, t.remarks
from Transaction t
join t.account a
where a.id = ?1
order by t.id desc
""")
    List<TransactionInfoDto> getByAccount(Long accountId, Pageable pageable);

    @Query("""
select t.referenceNumber, t.transactionType, t.transactionStatus, t.amount, a.accountNumber, t.destinationAccount, t.balance, t.timeStamp, t.remarks
from Transaction t
join t.account a
where (?1 is null OR a.accountNumber = ?1)
AND (?2 is null OR t.transactionType = ?2)
AND (?3 is null OR t.transactionStatus = ?3)
AND (?4 is null OR t.timeStamp >= ?4)
AND (?5 is null OR t.timeStamp <= ?5)
order by t.id desc
""")
    List<TransactionInfoDto> dynamicTransactionFilter(Long accountNumber, TransactionType transactionType, TransactionStatus transactionStatus, LocalDate from, LocalDate to, Pageable pageable);
}
