package com.springboot.BankingSystem.service;

import com.springboot.BankingSystem.dto.request.AdminDto;
import com.springboot.BankingSystem.enums.Role;
import com.springboot.BankingSystem.model.User;
import com.springboot.BankingSystem.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public void addAdmin(@Valid AdminDto adminDto) {
        User user = new User();
        user.setUsername(adminDto.username());
        user.setPassword(passwordEncoder.encode(adminDto.password()));
        user.setRole(Role.ADMIN);

        userRepository.save(user);
    }
}
