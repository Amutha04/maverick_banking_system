package com.springboot.BankingSystem.service;

import com.springboot.BankingSystem.dto.request.AdminDto;
import com.springboot.BankingSystem.dto.request.ChangePasswordDto;
import com.springboot.BankingSystem.dto.response.UserDetailsDto;
import com.springboot.BankingSystem.enums.JobTitle;
import com.springboot.BankingSystem.enums.Role;
import com.springboot.BankingSystem.exception.InActiveUserException;
import com.springboot.BankingSystem.exception.PasswordMismatchException;
import com.springboot.BankingSystem.model.User;
import com.springboot.BankingSystem.repository.ExecutiveRepository;
import com.springboot.BankingSystem.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ExecutiveRepository executiveRepository;

    public void addAdmin(@Valid AdminDto adminDto) {
        User user = new User();
        user.setUsername(adminDto.username());
        user.setPassword(passwordEncoder.encode(adminDto.password()));
        user.setRole(Role.ADMIN);

        userRepository.save(user);
        log.info("Admin is added");
    }

    public UserDetailsDto getUserDetails(String loggedUsername) {
        // get the user
        User user = userRepository.loadUserByUsername(loggedUsername, true);

        // get the jobTitle for executive
        JobTitle jobTitle = executiveRepository.getExecutiveJobTitle(loggedUsername);
        return new UserDetailsDto(user.getUsername(), user.isActive(), user.getRole(), jobTitle, user.getCreatedAt());
    }

    public void changePassword(@Valid ChangePasswordDto changePasswordDto, String loggedUsername) {
        // 1. get the logged-in user, the password belongs to the user not to the customer or executive
        User user = userRepository.loadUserByUsername(loggedUsername, true);

        // 2. the current password must match
        if(!passwordEncoder.matches(changePasswordDto.currentPassword(), user.getPassword()))
            throw new PasswordMismatchException("Current password is incorrect");

        // 3. the new password must be different
        if(changePasswordDto.currentPassword().equals(changePasswordDto.newPassword()))
            throw new IllegalArgumentException("New password must be different from the current one");

        // 4. encode and save
        user.setPassword(passwordEncoder.encode(changePasswordDto.newPassword()));
        userRepository.save(user);
        log.info("Password is changed");
    }
}
