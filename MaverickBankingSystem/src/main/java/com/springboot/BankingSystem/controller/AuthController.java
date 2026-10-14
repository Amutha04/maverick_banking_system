package com.springboot.BankingSystem.controller;

import com.springboot.BankingSystem.dto.response.TokenDto;
import com.springboot.BankingSystem.utility.JwtUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtility jwtUtility;

    @GetMapping("/api/auth/login")
    public TokenDto login(Principal principal) {
        String loggedInUsername = principal.getName();
        String token = jwtUtility.generateToken(loggedInUsername);
        return new TokenDto(
                token,
                jwtUtility.extractExpiration(token),
                LocalDateTime.now().toString()
        );
    }
}