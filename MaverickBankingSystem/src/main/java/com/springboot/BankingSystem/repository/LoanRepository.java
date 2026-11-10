package com.springboot.BankingSystem.repository;
import com.springboot.BankingSystem.dto.response.LoanInfoDto;
import com.springboot.BankingSystem.enums.LoanStatus;
import com.springboot.BankingSystem.enums.LoanType;
import com.springboot.BankingSystem.model.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
AND l.loanStatus != LoanStatus.CLOSED
AND l.loanStatus != LoanStatus.REJECTED
""")
    boolean getLoanByCustomerId(Long id);

    @Modifying
    @Transactional
    @Query("""
update Loan l
set l.loanStatus = LoanStatus.REJECTED, l.remark = ?2
where l.account.id = ?1
AND (l.loanStatus = LoanStatus.IN_PROCESS or l.loanStatus = LoanStatus.SANCTIONED)
""")
    void rejectNotOpenedLoans(Long id, String remark);

    @Query("""
select l.id, l.loanType, l.amount, l.interestRate, l.tenureMonths, l.purpose,l.loanStatus, l.outstandingAmount, l.requestedOn, l.processedOn, l.disbursedDate, l.closedOn, l.remark, a.accountNumber, c.name, e.name, e.email
from Loan l
join l.account a
join a.customer c
left join l.executive e
""")
    List<LoanInfoDto> getAll(Pageable pageable);

    @Query("""
select l.id, l.loanType, l.amount, l.interestRate, l.tenureMonths,l.purpose, l.loanStatus, l.outstandingAmount, l.requestedOn, l.processedOn, l.disbursedDate, l.closedOn, l.remark, a.accountNumber, c.name, e.name, e.email
from Loan l
join l.account a
join a.customer c
left join l.executive e
where c.id = ?1
""")
    List<LoanInfoDto> getByCustomer(Long customerId, Pageable pageable);

    @Query("""
select l.id, l.loanType, l.amount, l.interestRate, l.tenureMonths,l.purpose, l.loanStatus, l.outstandingAmount, l.requestedOn, l.processedOn, l.disbursedDate, l.closedOn, l.remark, a.accountNumber, c.name, e.name, e.email
from Loan l
join l.account a
join a.customer c
join c.user u
left join l.executive e
where u.username = ?1
""")
    List<LoanInfoDto> getByUsername(String username, Pageable pageable);

    @Query(value = """
select l.id, l.loanType, l.amount, l.interestRate, l.tenureMonths, l.purpose, l.loanStatus, l.outstandingAmount, l.requestedOn, l.processedOn, l.disbursedDate, l.closedOn, l.remark, a.accountNumber, c.name, e.name, e.email
from Loan l
join l.account a
join a.customer c
left join l.executive e
where (?1 is null OR a.branch.id = ?1)
AND (?2 is null OR e.id = ?2)
AND (?3 is null OR l.loanType = ?3)
AND (?4 is null OR l.loanStatus = ?4)
AND (?5 is null OR a.accountNumber = ?5)
AND (?6 is null OR c.name = ?6)
AND (?7 is null OR l.requestedOn >= ?7)
AND (?8 is null OR l.requestedOn <= ?8)
AND (?9 = false OR e.id is null)
""")
    Page<LoanInfoDto> dynamicLoanFilter(Long branchId, Long executiveId, LoanType loanType, LoanStatus loanStatus, Long accountNumber, String customerName, LocalDate requestedFrom, LocalDate requestedTo, boolean unassigned, Pageable pageable);

    @Query("""
select count(l.id)
from Loan l
join l.account a
where (?1 is null OR a.branch.id = ?1)
AND l.loanStatus = ?2
""")
    long getLoanCountByStatus(Long branchId, LoanStatus loanStatus);

    @Query("""
select count(l.id)
from Loan l
join l.executive e
where e.id = ?1
AND l.loanStatus in (LoanStatus.IN_PROCESS, LoanStatus.SANCTIONED, LoanStatus.ACCEPTED, LoanStatus.VERIFIED)
""")
    long getPendingCountByExecutive(Long executiveId);
}
