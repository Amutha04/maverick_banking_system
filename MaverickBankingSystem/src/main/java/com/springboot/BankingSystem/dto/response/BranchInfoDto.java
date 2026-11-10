package com.springboot.BankingSystem.dto.response;

import com.springboot.BankingSystem.enums.BranchName;
import com.springboot.BankingSystem.enums.District;
import com.springboot.BankingSystem.enums.IFSC;
import com.springboot.BankingSystem.enums.State;

public record BranchInfoDto(
        Long id,
        BranchName name,
        State state,
        District district,
        IFSC ifscCode
) {
}
