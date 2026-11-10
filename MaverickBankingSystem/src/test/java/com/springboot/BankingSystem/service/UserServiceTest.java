package com.springboot.BankingSystem.service;

import com.springboot.BankingSystem.dto.request.AdminDto;
import com.springboot.BankingSystem.dto.request.ChangePasswordDto;
import com.springboot.BankingSystem.enums.JobTitle;
import com.springboot.BankingSystem.enums.Role;
import com.springboot.BankingSystem.exception.PasswordMismatchException;
import com.springboot.BankingSystem.model.User;
import com.springboot.BankingSystem.repository.ExecutiveRepository;
import com.springboot.BankingSystem.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @InjectMocks
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ExecutiveRepository executiveRepository;

    @Test
    public void addAdminTest() {
        // adminDto comes from postman/react app via controller. here, I define it myself
        AdminDto adminDto = new AdminDto("admin", "admin@123");

        when(passwordEncoder.encode("admin@123")).thenReturn("encodedPassword");

        userService.addAdmin(adminDto);

        verify(passwordEncoder, times(1)).encode("admin@123");
        verify(userRepository, times(1)).save(any(User.class));

        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void getUserDetailsTest() {
        User user = new User();
        user.setUsername("arjun");
        user.setRole(Role.EXECUTIVE);

        when(userRepository.loadUserByUsername("arjun", true)).thenReturn(user);
        when(executiveRepository.getExecutiveJobTitle("arjun")).thenReturn(JobTitle.PERSONAL_BANKER);

        assertNotNull(userService.getUserDetails("arjun"));

        verify(userRepository, times(1)).loadUserByUsername("arjun", true);
        verify(executiveRepository, times(1)).getExecutiveJobTitle("arjun");

        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(executiveRepository);
    }

    @Test
    public void changePasswordTest() {
        ChangePasswordDto changePasswordDto = new ChangePasswordDto("ravi@123", "ravi@456");
        User user = new User();
        user.setUsername("ravi");
        user.setPassword("encodedOldPassword");

        when(userRepository.loadUserByUsername("ravi", true)).thenReturn(user);
        when(passwordEncoder.matches("ravi@123", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("ravi@456")).thenReturn("encodedNewPassword");

        userService.changePassword(changePasswordDto, "ravi");

        verify(userRepository, times(1)).loadUserByUsername("ravi", true);
        verify(passwordEncoder, times(1)).matches("ravi@123", "encodedOldPassword");
        verify(passwordEncoder, times(1)).encode("ravi@456");
        verify(userRepository, times(1)).save(user);

        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void changePasswordTest_Mismatch() {
        // current password given by the user is wrong
        ChangePasswordDto changePasswordDto = new ChangePasswordDto("wrong@123", "ravi@456");
        User user = new User();
        user.setUsername("ravi");
        user.setPassword("encodedOldPassword");

        when(userRepository.loadUserByUsername("ravi", true)).thenReturn(user);
        when(passwordEncoder.matches("wrong@123", "encodedOldPassword")).thenReturn(false);

        assertThrows(PasswordMismatchException.class, () -> userService.changePassword(changePasswordDto, "ravi"));

        verify(userRepository, times(1)).loadUserByUsername("ravi", true);
        verify(passwordEncoder, times(1)).matches("wrong@123", "encodedOldPassword");
        verify(passwordEncoder, times(0)).encode("ravi@456");
        verify(userRepository, times(0)).save(any(User.class));

        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void changePasswordTest_SamePassword() {
        // new password is the same as the current one
        ChangePasswordDto changePasswordDto = new ChangePasswordDto("ravi@123", "ravi@123");
        User user = new User();
        user.setUsername("ravi");
        user.setPassword("encodedOldPassword");

        when(userRepository.loadUserByUsername("ravi", true)).thenReturn(user);
        when(passwordEncoder.matches("ravi@123", "encodedOldPassword")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.changePassword(changePasswordDto, "ravi"));

        verify(userRepository, times(1)).loadUserByUsername("ravi", true);
        verify(passwordEncoder, times(1)).matches("ravi@123", "encodedOldPassword");
        verify(passwordEncoder, times(0)).encode("ravi@123");
        verify(userRepository, times(0)).save(any(User.class));

        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }
}
