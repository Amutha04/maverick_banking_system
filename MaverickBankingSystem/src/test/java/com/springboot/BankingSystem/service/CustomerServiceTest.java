package com.springboot.BankingSystem.service;

import com.springboot.BankingSystem.dto.request.CustomerDto;
import com.springboot.BankingSystem.dto.response.CustomerInfoDto;
import com.springboot.BankingSystem.dto.response.StatDto;
import com.springboot.BankingSystem.enums.BranchName;
import com.springboot.BankingSystem.enums.Gender;
import com.springboot.BankingSystem.enums.KYCStatus;
import com.springboot.BankingSystem.exception.DuplicateRequestException;
import com.springboot.BankingSystem.exception.InvalidAccountStateException;
import com.springboot.BankingSystem.exception.OwnershipException;
import com.springboot.BankingSystem.exception.ResourceNotFoundException;
import com.springboot.BankingSystem.model.Branch;
import com.springboot.BankingSystem.model.Customer;
import com.springboot.BankingSystem.model.Executive;
import com.springboot.BankingSystem.model.User;
import com.springboot.BankingSystem.repository.AccountRepository;
import com.springboot.BankingSystem.repository.CustomerRepository;
import com.springboot.BankingSystem.repository.ExecutiveRepository;
import com.springboot.BankingSystem.repository.LoanRepository;
import com.springboot.BankingSystem.repository.UserRepository;
import com.springboot.BankingSystem.utility.RoleCheckUtility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {
    @InjectMocks
    private CustomerService customerService;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RoleCheckUtility roleCheckUtility;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private LoanRepository loanRepository;
    @Mock
    private ExecutiveRepository executiveRepository;

    @Test
    public void getByIdTest() {
        User user = new User();
        user.setUsername("ravi");
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        assertNotNull(customerService.getById(1L, "admin"));

        verify(roleCheckUtility, times(1)).checkAdminOrExecutive("admin");
        verify(customerRepository, times(1)).findById(1L);

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    public void getByIdTest_Invalid() {
        when(customerRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.getById(5L, "admin"));

        verify(roleCheckUtility, times(1)).checkAdminOrExecutive("admin");
        verify(customerRepository, times(1)).findById(5L);

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    public void getByIdTest_InactiveUser() {
        // soft deleted customer, so the user is not active
        User user = new User();
        user.setActive(false);
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        assertThrows(ResourceNotFoundException.class, () -> customerService.getById(1L, "admin"));

        verify(roleCheckUtility, times(1)).checkAdminOrExecutive("admin");
        verify(customerRepository, times(1)).findById(1L);

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    public void addTest() {
        // customerDto comes from postman/react app via controller. here, I define it myself
        CustomerDto customerDto = new CustomerDto("Ravi Kumar", Gender.MALE, LocalDate.of(1990, 5, 15), "Anna Nagar, Chennai", "ravi@gmail.com", 9876543210L, "ravi", "ravi@123");

        when(userRepository.existsByUsername("ravi")).thenReturn(false);
        when(passwordEncoder.encode("ravi@123")).thenReturn("encodedPassword");

        customerService.add(customerDto);

        verify(userRepository, times(1)).existsByUsername("ravi");
        verify(passwordEncoder, times(1)).encode("ravi@123");
        verify(userRepository, times(1)).save(any(User.class));
        verify(customerRepository, times(1)).save(any(Customer.class));

        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    public void addTest_Invalid() {
        // username is already taken
        CustomerDto customerDto = new CustomerDto("Ravi Kumar", Gender.MALE, LocalDate.of(1990, 5, 15), "Anna Nagar, Chennai", "ravi@gmail.com", 9876543210L, "ravi", "ravi@123");

        when(userRepository.existsByUsername("ravi")).thenReturn(true);

        assertThrows(DuplicateRequestException.class, () -> customerService.add(customerDto));

        verify(userRepository, times(1)).existsByUsername("ravi");
        verify(passwordEncoder, times(0)).encode("ravi@123");
        verify(userRepository, times(0)).save(any(User.class));
        verify(customerRepository, times(0)).save(any(Customer.class));

        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    public void deleteTest() {
        User user = new User();
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(accountRepository.getAccountByCustomerId(1L)).thenReturn(false);
        when(loanRepository.getLoanByCustomerId(1L)).thenReturn(false);

        customerService.delete(1L, "admin");

        verify(roleCheckUtility, times(1)).checkAdmin("admin");
        verify(customerRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).getAccountByCustomerId(1L);
        verify(loanRepository, times(1)).getLoanByCustomerId(1L);
        verify(customerRepository, times(1)).save(customer);

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(loanRepository);
    }

    @Test
    public void deleteTest_Invalid() {
        when(customerRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.delete(5L, "admin"));

        verify(roleCheckUtility, times(1)).checkAdmin("admin");
        verify(customerRepository, times(1)).findById(5L);
        verify(accountRepository, times(0)).getAccountByCustomerId(5L);
        verify(loanRepository, times(0)).getLoanByCustomerId(5L);
        verify(customerRepository, times(0)).save(any(Customer.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(loanRepository);
    }

    @Test
    public void deleteTest_OpenAccounts() {
        Customer customer = new Customer();
        customer.setId(1L);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(accountRepository.getAccountByCustomerId(1L)).thenReturn(true);

        assertThrows(InvalidAccountStateException.class, () -> customerService.delete(1L, "admin"));

        verify(roleCheckUtility, times(1)).checkAdmin("admin");
        verify(customerRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).getAccountByCustomerId(1L);
        verify(loanRepository, times(0)).getLoanByCustomerId(1L);
        verify(customerRepository, times(0)).save(any(Customer.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(loanRepository);
    }

    @Test
    public void deleteTest_OpenLoans() {
        Customer customer = new Customer();
        customer.setId(1L);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(accountRepository.getAccountByCustomerId(1L)).thenReturn(false);
        when(loanRepository.getLoanByCustomerId(1L)).thenReturn(true);

        assertThrows(InvalidAccountStateException.class, () -> customerService.delete(1L, "admin"));

        verify(roleCheckUtility, times(1)).checkAdmin("admin");
        verify(customerRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).getAccountByCustomerId(1L);
        verify(loanRepository, times(1)).getLoanByCustomerId(1L);
        verify(customerRepository, times(0)).save(any(Customer.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(loanRepository);
    }

    @Test
    public void deleteTest_NotAdmin() {
        when(roleCheckUtility.checkAdmin("ravi")).thenThrow(OwnershipException.class);

        assertThrows(OwnershipException.class, () -> customerService.delete(1L, "ravi"));

        verify(roleCheckUtility, times(1)).checkAdmin("ravi");
        verify(customerRepository, times(0)).findById(1L);
        verify(customerRepository, times(0)).save(any(Customer.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(loanRepository);
    }

    @Test
    public void getAllTest() {
        CustomerInfoDto dto1 = new CustomerInfoDto(1L, "Ravi Kumar", Gender.MALE, LocalDate.of(1990, 5, 15), "ravi@gmail.com", 9876543210L, null, null, 750, KYCStatus.PENDING, "ravi", LocalDate.now());
        CustomerInfoDto dto2 = new CustomerInfoDto(2L, "Priya Sundaram", Gender.FEMALE, LocalDate.of(1995, 8, 20), "priya@gmail.com", 9876543211L, null, null, 680, KYCStatus.DONE, "priya", LocalDate.now());

        when(customerRepository.getAll(PageRequest.of(0, 5))).thenReturn(List.of(dto1, dto2));

        assertEquals(2, customerService.getAll(0, 5, "admin").size());

        verify(roleCheckUtility, times(1)).checkAdminOrExecutive("admin");
        verify(customerRepository, times(1)).getAll(PageRequest.of(0, 5));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    public void dynamicCustomerFilterTest() {
        CustomerInfoDto dto1 = new CustomerInfoDto(1L, "Ravi Kumar", Gender.MALE, LocalDate.of(1990, 5, 15), "ravi@gmail.com", 9876543210L, null, null, 750, KYCStatus.DONE, "ravi", LocalDate.now());

        // the repository gives back a page, so wrap the list in PageImpl
        when(customerRepository.dynamicCustomerFilter(null, null, null, null, KYCStatus.DONE, null, null, PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(dto1), PageRequest.of(0, 5), 1));

        assertEquals(1, customerService.dynamicCustomerFilter(null, null, null, null, KYCStatus.DONE, null, null, 0, 5, "admin").list().size());

        verify(roleCheckUtility, times(1)).checkAdminOrExecutive("admin");
        verify(customerRepository, times(1)).dynamicCustomerFilter(null, null, null, null, KYCStatus.DONE, null, null, PageRequest.of(0, 5));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    public void getTest() {
        User user = new User();
        user.setUsername("ravi");
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);

        when(customerRepository.findByUsername("ravi")).thenReturn(customer);

        assertNotNull(customerService.get("ravi"));

        verify(customerRepository, times(1)).findByUsername("ravi");

        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    public void updateKYCTest() {
        Customer customer = new Customer();
        customer.setId(1L);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        customerService.updateKYC(1L, "arjun");

        verify(roleCheckUtility, times(1)).checkExecutive("arjun");
        verify(customerRepository, times(1)).findById(1L);
        verify(customerRepository, times(1)).save(customer);

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    public void updateKYCTest_Invalid() {
        when(customerRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> customerService.updateKYC(5L, "arjun"));

        verify(roleCheckUtility, times(1)).checkExecutive("arjun");
        verify(customerRepository, times(1)).findById(5L);
        verify(customerRepository, times(0)).save(any(Customer.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    public void getCustomersByKycTest_Executive() {
        // executive sees only the customers of the own branch
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setName(BranchName.ANNA_NAGAR);
        Executive executive = new Executive();
        executive.setId(1L);
        executive.setBranch(branch);

        when(executiveRepository.findByUsername("arjun")).thenReturn(executive);
        when(customerRepository.getCustomerCountByKyc("ANNA_NAGAR", KYCStatus.PENDING)).thenReturn(4L);
        when(customerRepository.getCustomerCountByKyc("ANNA_NAGAR", KYCStatus.DONE)).thenReturn(2L);

        StatDto statDto = customerService.getCustomersByKyc("arjun");
        assertEquals(List.of(4L, 2L), statDto.values());

        verify(roleCheckUtility, times(1)).checkAdminOrExecutive("arjun");
        verify(executiveRepository, times(1)).findByUsername("arjun");
        verify(customerRepository, times(1)).getCustomerCountByKyc("ANNA_NAGAR", KYCStatus.PENDING);
        verify(customerRepository, times(1)).getCustomerCountByKyc("ANNA_NAGAR", KYCStatus.DONE);

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(customerRepository);
    }

    @Test
    public void getCustomersByKycTest_Admin() {
        // admin is not an executive, so branch is null and all customers are counted
        when(executiveRepository.findByUsername("admin")).thenReturn(null);
        when(customerRepository.getCustomerCountByKyc(null, KYCStatus.PENDING)).thenReturn(10L);
        when(customerRepository.getCustomerCountByKyc(null, KYCStatus.DONE)).thenReturn(6L);

        StatDto statDto = customerService.getCustomersByKyc("admin");
        assertEquals(List.of(10L, 6L), statDto.values());

        verify(roleCheckUtility, times(1)).checkAdminOrExecutive("admin");
        verify(executiveRepository, times(1)).findByUsername("admin");
        verify(customerRepository, times(1)).getCustomerCountByKyc(null, KYCStatus.PENDING);
        verify(customerRepository, times(1)).getCustomerCountByKyc(null, KYCStatus.DONE);

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(customerRepository);
    }
}
