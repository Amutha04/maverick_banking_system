package com.springboot.BankingSystem.dto.response;

import com.springboot.BankingSystem.enums.BranchName;
import com.springboot.BankingSystem.enums.District;
import com.springboot.BankingSystem.enums.IFSC;
import com.springboot.BankingSystem.enums.State;

public record AccountRespDto(
        String name,
        Long accountNumber,
        IFSC ifscCode,
        BranchName branchName,
        District branchDistrict,
        State branchState
) {
}
