package com.springboot.BankingSystem.mapper;

import com.springboot.BankingSystem.dto.request.BeneficiaryTransactionDto;
import com.springboot.BankingSystem.dto.request.TransactionDto;
import com.springboot.BankingSystem.model.Beneficiaries;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {
    public static TransactionDto convertBeneficiaryToTransactionDto(BeneficiaryTransactionDto beneficiaryTransactionDto, Beneficiaries beneficiary) {
        return new TransactionDto(
                beneficiaryTransactionDto.amount(),
                beneficiary.getAccountNumber(),
                beneficiaryTransactionDto.password(),
                beneficiaryTransactionDto.remarks()
        );
    }
}
