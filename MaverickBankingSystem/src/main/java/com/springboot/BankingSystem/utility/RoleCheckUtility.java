package com.springboot.BankingSystem.utility;

import com.springboot.BankingSystem.enums.JobTitle;
import com.springboot.BankingSystem.enums.Role;
import com.springboot.BankingSystem.exception.OwnershipException;
import com.springboot.BankingSystem.model.User;
import com.springboot.BankingSystem.repository.ExecutiveRepository;
import com.springboot.BankingSystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleCheckUtility {
    private final UserRepository userRepository;
    private final ExecutiveRepository executiveRepository;

    public User checkCustomer(String loggedUsername) {
        User user = userRepository.loadUserByUsername(loggedUsername, true);
        if(!user.getRole().equals(Role.CUSTOMER))
            throw new OwnershipException("Operation not allowed.");
        return user;
    }

    public User checkExecutive(String loggedUsername) {
        User user = userRepository.loadUserByUsername(loggedUsername, true);
        if(!user.getRole().equals(Role.EXECUTIVE))
            throw new OwnershipException("Operation not allowed.");
        return user;
    }

    public User checkAdmin(String loggedUsername) {
        User user = userRepository.loadUserByUsername(loggedUsername, true);
        if(!user.getRole().equals(Role.ADMIN))
            throw new OwnershipException("Operation not allowed.");
        return user;
    }

    public User checkAdminOrExecutive(String loggedUsername) {
        User user = userRepository.loadUserByUsername(loggedUsername, true);
        if(user.getRole().equals(Role.CUSTOMER))
            throw new OwnershipException("Operation not allowed.");
        return user;
    }

    public User checkManager(String loggedUsername) {
        User user = userRepository.loadUserByUsername(loggedUsername, true);
        JobTitle jobTitle = executiveRepository.getExecutiveJobTitle(loggedUsername);

        if(!(user.getRole().equals(Role.EXECUTIVE) && jobTitle == JobTitle.BANK_MANAGER))
            throw new OwnershipException("Operation not allowed.");
        return user;
    }

}
