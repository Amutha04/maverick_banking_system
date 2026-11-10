package com.springboot.BankingSystem.controller;

import com.springboot.BankingSystem.dto.response.BranchInfoDto;
import com.springboot.BankingSystem.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class BranchController {

    private final BranchService branchService;

    @GetMapping("/api/branch/all")
    public List<BranchInfoDto> getAll() {
        return branchService.getAll();
    }
}
