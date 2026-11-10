package com.springboot.BankingSystem.service;

import com.springboot.BankingSystem.dto.response.BranchInfoDto;
import com.springboot.BankingSystem.model.User;
import com.springboot.BankingSystem.repository.BranchRepository;
import com.springboot.BankingSystem.utility.RoleCheckUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchService {
    private final BranchRepository branchRepository;

    public List<BranchInfoDto> getAll() {
        // get all branches
        return branchRepository.getAll();
    }
}
