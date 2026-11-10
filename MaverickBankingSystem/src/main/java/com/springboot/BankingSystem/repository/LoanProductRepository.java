package com.springboot.BankingSystem.repository;

import com.springboot.BankingSystem.dto.response.LoanProductDto;
import com.springboot.BankingSystem.enums.LoanType;
import com.springboot.BankingSystem.model.LoanProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface LoanProductRepository extends JpaRepository<LoanProduct, Long> {
    @Query("""
select l.interestRate
from LoanProduct l
where l.loanType = ?1
""")
    BigDecimal getByLoanType(LoanType loanType);


    @Query("""
select l.id, l.loanType, l.maxAmount, l.interestRate, l.tenureMonths
from LoanProduct l
""")
    List<LoanProductDto> getLoanProducts();
}
