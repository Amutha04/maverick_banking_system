package com.springboot.BankingSystem.controller;

import com.springboot.BankingSystem.dto.request.ChangePasswordDto;
import com.springboot.BankingSystem.dto.response.TokenDto;
import com.springboot.BankingSystem.dto.response.UserDetailsDto;
import com.springboot.BankingSystem.repository.UserRepository;
import com.springboot.BankingSystem.service.UserService;
import com.springboot.BankingSystem.utility.JwtUtility;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final JwtUtility jwtUtility;
    private final UserService userService;

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

    @GetMapping("/api/auth/user-details")
    public UserDetailsDto getUserDetails(Principal principal) {
        return userService.getUserDetails(principal.getName());
    }

    @PutMapping("/api/auth/change-password")
    public void changePassword(@Valid @RequestBody ChangePasswordDto changePasswordDto,
                               Principal principal) {
        userService.changePassword(changePasswordDto, principal.getName());
    }
}
