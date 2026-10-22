package com.springboot.BankingSystem.repository;
import com.springboot.BankingSystem.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
}
