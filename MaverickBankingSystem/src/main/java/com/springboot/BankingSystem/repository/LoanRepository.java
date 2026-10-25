package com.springboot.BankingSystem.repository;
import com.springboot.BankingSystem.dto.response.LoanInfoDto;
import com.springboot.BankingSystem.enums.LoanStatus;
import com.springboot.BankingSystem.enums.LoanType;
import com.springboot.BankingSystem.model.Loan;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    @Query("""
select count(l.id) > 0
from Loan l
join l.account a
where a.id = ?1
AND l.loanStatus = LoanStatus.OPEN
""")
    boolean checkForOpenLoan(Long id);

    @Query("""
select count(l.id) > 0
from Loan l
join l.account a
where a.id = ?1
AND l.loanStatus = LoanStatus.IN_PROCESS
""")
    boolean checkForPendingLoan(Long id);

    @Query("""
select count(l.id) > 0
from Loan l
join l.account a
join a.customer c
where c.id = ?1
AND (l.loanStatus != LoanStatus.CLOSED OR l.loanStatus != LoanStatus.REJECTED)
""")
    boolean getLoanByCustomerId(Long id);

    @Query("""
update Loan l
join l.account a
set l.loanStatus = LoanStatus.REJECTED, l.remark = ?2
where a.id = ?1
AND (l.loanStatus = loanStatus.IN_PROCESS or l.loanStatus = loanStatus.SANCTIONED)
""")
    void rejectNotOpenedLoans(Long id, String remark);

    @Query("""
select l.id, l.loanType, l.amount, l.interestRate, l.tenureMonths, l.loanStatus, l.outstandingAmount, l.requestedOn, l.processedOn, l.disbursedDate, l.closedOn, l.remark, a.accountNumber, c.name, e.name
from Loan l
join l.account a
join a.customer c
left join l.executive e
""")
    List<LoanInfoDto> getAll(Pageable pageable);

    @Query("""
select l.id, l.loanType, l.amount, l.interestRate, l.tenureMonths, l.loanStatus, l.outstandingAmount, l.requestedOn, l.processedOn, l.disbursedDate, l.closedOn, l.remark, a.accountNumber, c.name, e.name
from Loan l
join l.account a
join a.customer c
left join l.executive e
where c.id = ?1
""")
    List<LoanInfoDto> getByCustomer(Long customerId, Pageable pageable);

    @Query("""
select l.id, l.loanType, l.amount, l.interestRate, l.tenureMonths, l.loanStatus, l.outstandingAmount, l.requestedOn, l.processedOn, l.disbursedDate, l.closedOn, l.remark, a.accountNumber, c.name, e.name
from Loan l
join l.account a
join a.customer c
join c.user u
left join l.executive e
where u.username = ?1
""")
    List<LoanInfoDto> getByUsername(String username, Pageable pageable);

    @Query("""
select l.id, l.loanType, l.amount, l.interestRate, l.tenureMonths, l.loanStatus, l.outstandingAmount, l.requestedOn, l.processedOn, l.disbursedDate, l.closedOn, l.remark, a.accountNumber, c.name, e.name
from Loan l
join l.account a
join a.customer c
left join l.executive e
where (?1 is null OR l.loanType = ?1)
AND (?2 is null OR l.loanStatus = ?2)
AND (?3 is null OR a.accountNumber = ?3)
AND (?4 is null OR l.requestedOn >= ?4)
AND (?5 is null OR l.requestedOn <= ?5)
""")
    List<LoanInfoDto> dynamicLoanFilter(LoanType loanType, LoanStatus loanStatus, Long accountNumber, LocalDate requestedFrom, LocalDate requestedTo, Pageable pageable);
}
