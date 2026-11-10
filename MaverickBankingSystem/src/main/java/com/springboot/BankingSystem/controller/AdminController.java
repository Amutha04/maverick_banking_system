package com.springboot.BankingSystem.controller;

import com.springboot.BankingSystem.dto.request.AdminDto;
import com.springboot.BankingSystem.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class AdminController {

    private final UserService userService;

    @PostMapping("/api/admin/add")
    public void addAdmin(@Valid @RequestBody AdminDto adminDto) {
        userService.addAdmin(adminDto);
    }
}