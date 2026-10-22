package com.springboot.BankingSystem.mapper;

import com.springboot.BankingSystem.dto.response.LoanInfoDto;
import com.springboot.BankingSystem.model.Loan;
import org.springframework.stereotype.Component;

@Component
public class LoanMapper {
    public static LoanInfoDto convertLoanToDto(Loan loan) {
        return new LoanInfoDto(
                loan.getId(),
                loan.getLoanType(),
                loan.getAmount(),
                loan.getInterestRate(),
                loan.getTenureMonths(),
                loan.getLoanStatus(),
                loan.getOutstandingAmount(),
                loan.getRequestedOn(),
                loan.getProcessedOn(),
                loan.getDisbursedDate(),
                loan.getClosedOn(),
                loan.getRemark(),
                loan.getAccount().getAccountNumber(),
                loan.getAccount().getCustomer().getName(),
                loan.getExecutive() == null ? null : loan.getExecutive().getName()
        );
    }
}
