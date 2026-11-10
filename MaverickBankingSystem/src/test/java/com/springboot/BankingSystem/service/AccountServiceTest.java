package com.springboot.BankingSystem.service;

import com.springboot.BankingSystem.dto.request.BalanceDto;
import com.springboot.BankingSystem.dto.request.DepositDto;
import com.springboot.BankingSystem.dto.request.PasswordDto;
import com.springboot.BankingSystem.dto.request.WithdrawDto;
import com.springboot.BankingSystem.dto.response.AccountInfoDto;
import com.springboot.BankingSystem.dto.response.BalanceInfoDto;
import com.springboot.BankingSystem.dto.response.StatDto;
import com.springboot.BankingSystem.enums.AccountStatus;
import com.springboot.BankingSystem.enums.AccountType;
import com.springboot.BankingSystem.enums.BranchName;
import com.springboot.BankingSystem.enums.District;
import com.springboot.BankingSystem.enums.IFSC;
import com.springboot.BankingSystem.exception.InsufficientBalanceException;
import com.springboot.BankingSystem.exception.InvalidAccountStateException;
import com.springboot.BankingSystem.exception.OwnershipException;
import com.springboot.BankingSystem.exception.PasswordMismatchException;
import com.springboot.BankingSystem.exception.ResourceNotFoundException;
import com.springboot.BankingSystem.model.Account;
import com.springboot.BankingSystem.model.Branch;
import com.springboot.BankingSystem.model.Customer;
import com.springboot.BankingSystem.model.Executive;
import com.springboot.BankingSystem.model.Transaction;
import com.springboot.BankingSystem.model.User;
import com.springboot.BankingSystem.repository.AccountRepository;
import com.springboot.BankingSystem.repository.BranchRepository;
import com.springboot.BankingSystem.repository.CustomerRepository;
import com.springboot.BankingSystem.repository.ExecutiveRepository;
import com.springboot.BankingSystem.repository.TransactionRepository;
import com.springboot.BankingSystem.utility.RoleCheckUtility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
    @InjectMocks
    private AccountService accountService;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private RoleCheckUtility roleCheckUtility;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TransactionService transactionService;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private ExecutiveRepository executiveRepository;
    @Mock
    private BranchRepository branchRepository;

    @Test
    public void getTest() {
        User user = new User();
        user.setUsername("ravi");
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("Ravi Kumar");
        customer.setUser(user);
        // the mapper reads the branch details, so the account needs a branch
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setName(BranchName.ANNA_NAGAR);
        Account account = new Account();
        account.setId(1L);
        account.setCustomer(customer);
        account.setBranch(branch);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertNotNull(accountService.get(1L, "ravi"));

        verify(accountRepository, times(1)).findById(1L);

        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void getTest_Invalid() {
        when(accountRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.get(5L, "ravi"));

        verify(accountRepository, times(1)).findById(5L);

        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void getTest_NotOwner() {
        // account belongs to ravi, but priya is asking for it
        User user = new User();
        user.setUsername("ravi");
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Account account = new Account();
        account.setId(1L);
        account.setCustomer(customer);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThrows(OwnershipException.class, () -> accountService.get(1L, "priya"));

        verify(accountRepository, times(1)).findById(1L);

        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void getAllTest() {
        AccountInfoDto dto1 = new AccountInfoDto(1L, "Ravi Kumar", 1000000001L, AccountType.SAVINGS, new BigDecimal("1000"), IFSC.DEMO0001001, BranchName.ANNA_NAGAR, District.CHENNAI, AccountStatus.ACTIVE, LocalDate.now(), "Arjun", "arjun@gmail.com");
        AccountInfoDto dto2 = new AccountInfoDto(2L, "Priya Sundaram", 1000000002L, AccountType.CHECKING, new BigDecimal("2500"), IFSC.DEMO0001002, BranchName.T_NAGAR, District.CHENNAI, AccountStatus.ACTIVE, LocalDate.now(), "Arjun", "arjun@gmail.com");

        when(accountRepository.getAll(PageRequest.of(0, 5))).thenReturn(List.of(dto1, dto2));

        assertEquals(2, accountService.getAll(0, 5, "admin").size());

        verify(roleCheckUtility, times(1)).checkAdminOrExecutive("admin");
        verify(accountRepository, times(1)).getAll(PageRequest.of(0, 5));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void getByCustomerTest() {
        Customer customer = new Customer();
        customer.setId(1L);
        AccountInfoDto dto1 = new AccountInfoDto(1L, "Ravi Kumar", 1000000001L, AccountType.SAVINGS, new BigDecimal("1000"), IFSC.DEMO0001001, BranchName.ANNA_NAGAR, District.CHENNAI, AccountStatus.ACTIVE, LocalDate.now(), "Arjun", "arjun@gmail.com");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(accountRepository.getByCustomer(1L, PageRequest.of(0, 5))).thenReturn(List.of(dto1));

        assertEquals(1, accountService.getByCustomer(0, 5, 1L, "admin").size());

        verify(roleCheckUtility, times(1)).checkAdminOrExecutive("admin");
        verify(customerRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).getByCustomer(1L, PageRequest.of(0, 5));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void getByCustomerTest_Invalid() {
        when(customerRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.getByCustomer(0, 5, 5L, "admin"));

        verify(roleCheckUtility, times(1)).checkAdminOrExecutive("admin");
        verify(customerRepository, times(1)).findById(5L);
        verify(accountRepository, times(0)).getByCustomer(5L, PageRequest.of(0, 5));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void suspendTest() {
        Account account = new Account();
        account.setId(1L);
        account.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        accountService.suspend(1L, "admin");

        verify(roleCheckUtility, times(1)).checkAdmin("admin");
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).save(account);

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void suspendTest_Invalid() {
        when(accountRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.suspend(5L, "admin"));

        verify(roleCheckUtility, times(1)).checkAdmin("admin");
        verify(accountRepository, times(1)).findById(5L);
        verify(accountRepository, times(0)).save(any(Account.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void suspendTest_NotActive() {
        // already suspended account cannot be suspended again
        Account account = new Account();
        account.setId(1L);
        account.setStatus(AccountStatus.SUSPENDED);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThrows(InvalidAccountStateException.class, () -> accountService.suspend(1L, "admin"));

        verify(roleCheckUtility, times(1)).checkAdmin("admin");
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(0)).save(any(Account.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void suspendTest_NotAdmin() {
        when(roleCheckUtility.checkAdmin("ravi")).thenThrow(OwnershipException.class);

        assertThrows(OwnershipException.class, () -> accountService.suspend(1L, "ravi"));

        verify(roleCheckUtility, times(1)).checkAdmin("ravi");
        verify(accountRepository, times(0)).findById(1L);
        verify(accountRepository, times(0)).save(any(Account.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void activateTest() {
        Account account = new Account();
        account.setId(1L);
        account.setStatus(AccountStatus.SUSPENDED);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        accountService.activate(1L, "admin");

        verify(roleCheckUtility, times(1)).checkAdmin("admin");
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).save(account);

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void activateTest_Invalid() {
        when(accountRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.activate(5L, "admin"));

        verify(roleCheckUtility, times(1)).checkAdmin("admin");
        verify(accountRepository, times(1)).findById(5L);
        verify(accountRepository, times(0)).save(any(Account.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void activateTest_NotSuspended() {
        // only a suspended account can be activated
        Account account = new Account();
        account.setId(1L);
        account.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThrows(InvalidAccountStateException.class, () -> accountService.activate(1L, "admin"));

        verify(roleCheckUtility, times(1)).checkAdmin("admin");
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(0)).save(any(Account.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void setPasswordTest() {
        PasswordDto passwordDto = new PasswordDto("1234");
        User user = new User();
        user.setUsername("ravi");
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Account account = new Account();
        account.setId(1L);
        account.setStatus(AccountStatus.ACTIVE);
        account.setCustomer(customer);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(passwordEncoder.encode("1234")).thenReturn("encodedPassword");

        accountService.setPassword(1L, passwordDto, "ravi");

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(accountRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).encode("1234");
        verify(accountRepository, times(1)).save(account);

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void setPasswordTest_Invalid() {
        PasswordDto passwordDto = new PasswordDto("1234");

        when(accountRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.setPassword(5L, passwordDto, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(accountRepository, times(1)).findById(5L);
        verify(passwordEncoder, times(0)).encode("1234");
        verify(accountRepository, times(0)).save(any(Account.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void setPasswordTest_NotOwner() {
        // account belongs to ravi, but priya is trying to set the password
        PasswordDto passwordDto = new PasswordDto("1234");
        User user = new User();
        user.setUsername("ravi");
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Account account = new Account();
        account.setId(1L);
        account.setStatus(AccountStatus.ACTIVE);
        account.setCustomer(customer);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThrows(OwnershipException.class, () -> accountService.setPassword(1L, passwordDto, "priya"));

        verify(roleCheckUtility, times(1)).checkCustomer("priya");
        verify(accountRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(0)).encode("1234");
        verify(accountRepository, times(0)).save(any(Account.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void setPasswordTest_NotActive() {
        PasswordDto passwordDto = new PasswordDto("1234");
        User user = new User();
        user.setUsername("ravi");
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Account account = new Account();
        account.setId(1L);
        account.setStatus(AccountStatus.SUSPENDED);
        account.setCustomer(customer);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThrows(InvalidAccountStateException.class, () -> accountService.setPassword(1L, passwordDto, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(accountRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(0)).encode("1234");
        verify(accountRepository, times(0)).save(any(Account.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void withdrawTest() {
        WithdrawDto withdrawDto = new WithdrawDto(new BigDecimal("500"), "1234", "rent");
        User user = new User();
        user.setUsername("ravi");
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber(1000000001L);
        account.setBalance(new BigDecimal("1000"));
        account.setPassword("encodedPassword");
        account.setStatus(AccountStatus.ACTIVE);
        account.setCustomer(customer);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("1234", "encodedPassword")).thenReturn(true);
        // reference number comes from the transaction service
        when(transactionService.generateReferenceNumber()).thenReturn("TXN123456789012");

        accountService.withdraw(1L, withdrawDto, "ravi");

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(accountRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).matches("1234", "encodedPassword");
        verify(transactionService, times(1)).generateReferenceNumber();
        verify(accountRepository, times(1)).save(account);
        verify(transactionRepository, times(1)).save(any(Transaction.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(transactionService);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    public void withdrawTest_Invalid() {
        WithdrawDto withdrawDto = new WithdrawDto(new BigDecimal("500"), "1234", "rent");

        when(accountRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.withdraw(5L, withdrawDto, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(accountRepository, times(1)).findById(5L);
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(transactionRepository, times(0)).save(any(Transaction.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    public void withdrawTest_ZeroAmount() {
        // amount is checked before the account is fetched
        WithdrawDto withdrawDto = new WithdrawDto(BigDecimal.ZERO, "1234", "rent");

        assertThrows(IllegalArgumentException.class, () -> accountService.withdraw(1L, withdrawDto, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(accountRepository, times(0)).findById(1L);
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(transactionRepository, times(0)).save(any(Transaction.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    public void withdrawTest_NotOwner() {
        // account belongs to ravi, but priya is trying to withdraw
        WithdrawDto withdrawDto = new WithdrawDto(new BigDecimal("500"), "1234", "rent");
        User user = new User();
        user.setUsername("ravi");
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Account account = new Account();
        account.setId(1L);
        account.setBalance(new BigDecimal("1000"));
        account.setPassword("encodedPassword");
        account.setStatus(AccountStatus.ACTIVE);
        account.setCustomer(customer);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThrows(OwnershipException.class, () -> accountService.withdraw(1L, withdrawDto, "priya"));

        verify(roleCheckUtility, times(1)).checkCustomer("priya");
        verify(accountRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(0)).matches("1234", "encodedPassword");
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(transactionRepository, times(0)).save(any(Transaction.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    public void withdrawTest_NotActive() {
        WithdrawDto withdrawDto = new WithdrawDto(new BigDecimal("500"), "1234", "rent");
        User user = new User();
        user.setUsername("ravi");
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Account account = new Account();
        account.setId(1L);
        account.setBalance(new BigDecimal("1000"));
        account.setPassword("encodedPassword");
        account.setStatus(AccountStatus.SUSPENDED);
        account.setCustomer(customer);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThrows(InvalidAccountStateException.class, () -> accountService.withdraw(1L, withdrawDto, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(accountRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(0)).matches("1234", "encodedPassword");
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(transactionRepository, times(0)).save(any(Transaction.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    public void withdrawTest_WrongPassword() {
        WithdrawDto withdrawDto = new WithdrawDto(new BigDecimal("500"), "9999", "rent");
        User user = new User();
        user.setUsername("ravi");
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Account account = new Account();
        account.setId(1L);
        account.setBalance(new BigDecimal("1000"));
        account.setPassword("encodedPassword");
        account.setStatus(AccountStatus.ACTIVE);
        account.setCustomer(customer);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("9999", "encodedPassword")).thenReturn(false);

        assertThrows(PasswordMismatchException.class, () -> accountService.withdraw(1L, withdrawDto, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(accountRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).matches("9999", "encodedPassword");
        verify(transactionService, times(0)).generateReferenceNumber();
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(transactionRepository, times(0)).save(any(Transaction.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(transactionService);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    public void withdrawTest_InsufficientBalance() {
        // balance is less than the amount, so only a failed transaction is saved
        WithdrawDto withdrawDto = new WithdrawDto(new BigDecimal("500"), "1234", "rent");
        User user = new User();
        user.setUsername("ravi");
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber(1000000001L);
        account.setBalance(new BigDecimal("100"));
        account.setPassword("encodedPassword");
        account.setStatus(AccountStatus.ACTIVE);
        account.setCustomer(customer);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("1234", "encodedPassword")).thenReturn(true);
        when(transactionService.generateReferenceNumber()).thenReturn("TXN123456789012");

        assertThrows(InsufficientBalanceException.class, () -> accountService.withdraw(1L, withdrawDto, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(accountRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).matches("1234", "encodedPassword");
        verify(transactionService, times(1)).generateReferenceNumber();
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(accountRepository, times(0)).save(any(Account.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(transactionService);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    public void depositTest() {
        DepositDto depositDto = new DepositDto(new BigDecimal("500"), "salary");
        User user = new User();
        user.setUsername("ravi");
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber(1000000001L);
        account.setBalance(new BigDecimal("1000"));
        account.setStatus(AccountStatus.ACTIVE);
        account.setCustomer(customer);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionService.generateReferenceNumber()).thenReturn("TXN123456789012");

        accountService.deposit(1L, depositDto, "ravi");

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).save(account);
        verify(transactionService, times(1)).generateReferenceNumber();
        verify(transactionRepository, times(1)).save(any(Transaction.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionService);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    public void depositTest_Invalid() {
        DepositDto depositDto = new DepositDto(new BigDecimal("500"), "salary");

        when(accountRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.deposit(5L, depositDto, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(accountRepository, times(1)).findById(5L);
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(transactionRepository, times(0)).save(any(Transaction.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    public void depositTest_NegativeAmount() {
        // amount is checked before the account is fetched
        DepositDto depositDto = new DepositDto(new BigDecimal("-100"), "salary");

        assertThrows(IllegalArgumentException.class, () -> accountService.deposit(1L, depositDto, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(accountRepository, times(0)).findById(1L);
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(transactionRepository, times(0)).save(any(Transaction.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    public void depositTest_NotOwner() {
        // account belongs to ravi, but priya is trying to deposit
        DepositDto depositDto = new DepositDto(new BigDecimal("500"), "salary");
        User user = new User();
        user.setUsername("ravi");
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Account account = new Account();
        account.setId(1L);
        account.setBalance(new BigDecimal("1000"));
        account.setStatus(AccountStatus.ACTIVE);
        account.setCustomer(customer);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThrows(OwnershipException.class, () -> accountService.deposit(1L, depositDto, "priya"));

        verify(roleCheckUtility, times(1)).checkCustomer("priya");
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(transactionRepository, times(0)).save(any(Transaction.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    public void depositTest_NotActive() {
        // deposit is allowed only for active accounts, not for closure request
        DepositDto depositDto = new DepositDto(new BigDecimal("500"), "salary");
        User user = new User();
        user.setUsername("ravi");
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Account account = new Account();
        account.setId(1L);
        account.setBalance(new BigDecimal("1000"));
        account.setStatus(AccountStatus.CLOSURE_REQUEST);
        account.setCustomer(customer);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThrows(InvalidAccountStateException.class, () -> accountService.deposit(1L, depositDto, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(transactionRepository, times(0)).save(any(Transaction.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    public void getByUsernameTest() {
        AccountInfoDto dto1 = new AccountInfoDto(1L, "Ravi Kumar", 1000000001L, AccountType.SAVINGS, new BigDecimal("1000"), IFSC.DEMO0001001, BranchName.ANNA_NAGAR, District.CHENNAI, AccountStatus.ACTIVE, LocalDate.now(), "Arjun", "arjun@gmail.com");
        AccountInfoDto dto2 = new AccountInfoDto(2L, "Ravi Kumar", 1000000002L, AccountType.CHECKING, new BigDecimal("2500"), IFSC.DEMO0001001, BranchName.ANNA_NAGAR, District.CHENNAI, AccountStatus.ACTIVE, LocalDate.now(), "Arjun", "arjun@gmail.com");

        when(accountRepository.getByUsername("ravi")).thenReturn(List.of(dto1, dto2));

        assertEquals(2, accountService.getByUsername("ravi").size());

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(accountRepository, times(1)).getByUsername("ravi");

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void checkBalanceTest() {
        BalanceDto balanceDto = new BalanceDto(1L, "1234");
        User user = new User();
        user.setUsername("ravi");
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Account account = new Account();
        account.setId(1L);
        account.setBalance(new BigDecimal("1000"));
        account.setPassword("encodedPassword");
        account.setCustomer(customer);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("1234", "encodedPassword")).thenReturn(true);

        BalanceInfoDto balanceInfoDto = accountService.checkBalance(balanceDto, "ravi");
        assertEquals(new BigDecimal("1000"), balanceInfoDto.balance());

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(accountRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).matches("1234", "encodedPassword");

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void checkBalanceTest_Invalid() {
        BalanceDto balanceDto = new BalanceDto(5L, "1234");

        when(accountRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.checkBalance(balanceDto, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(accountRepository, times(1)).findById(5L);

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void checkBalanceTest_NotOwner() {
        // account belongs to ravi, but priya is asking for the balance
        BalanceDto balanceDto = new BalanceDto(1L, "1234");
        User user = new User();
        user.setUsername("ravi");
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Account account = new Account();
        account.setId(1L);
        account.setBalance(new BigDecimal("1000"));
        account.setPassword("encodedPassword");
        account.setCustomer(customer);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThrows(OwnershipException.class, () -> accountService.checkBalance(balanceDto, "priya"));

        verify(roleCheckUtility, times(1)).checkCustomer("priya");
        verify(accountRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(0)).matches("1234", "encodedPassword");

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void checkBalanceTest_WrongPassword() {
        BalanceDto balanceDto = new BalanceDto(1L, "9999");
        User user = new User();
        user.setUsername("ravi");
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Account account = new Account();
        account.setId(1L);
        account.setBalance(new BigDecimal("1000"));
        account.setPassword("encodedPassword");
        account.setCustomer(customer);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("9999", "encodedPassword")).thenReturn(false);

        assertThrows(PasswordMismatchException.class, () -> accountService.checkBalance(balanceDto, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(accountRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).matches("9999", "encodedPassword");

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    public void dynamicAccountFilterTest_Executive() {
        // executive sees only the accounts of the own branch
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setName(BranchName.ANNA_NAGAR);
        Executive executive = new Executive();
        executive.setId(1L);
        executive.setBranch(branch);
        AccountInfoDto dto1 = new AccountInfoDto(1L, "Ravi Kumar", 1000000001L, AccountType.SAVINGS, new BigDecimal("1000"), IFSC.DEMO0001001, BranchName.ANNA_NAGAR, District.CHENNAI, AccountStatus.ACTIVE, LocalDate.now(), "Arjun", "arjun@gmail.com");

        when(executiveRepository.findByUsername("arjun")).thenReturn(executive);
        // the repository gives back a page, so wrap the list in PageImpl
        when(accountRepository.dynamicAccountFilter(1L, null, null, AccountStatus.ACTIVE, null, PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(dto1), PageRequest.of(0, 5), 1));

        assertEquals(1, accountService.dynamicAccountFilter(null, null, null, AccountStatus.ACTIVE, null, 0, 5, "arjun").list().size());

        verify(roleCheckUtility, times(1)).checkAdminOrExecutive("arjun");
        verify(executiveRepository, times(1)).findByUsername("arjun");
        verify(accountRepository, times(1)).dynamicAccountFilter(1L, null, null, AccountStatus.ACTIVE, null, PageRequest.of(0, 5));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void dynamicAccountFilterTest_Admin() {
        // admin is not an executive, so the branch chosen in the filter is used
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setName(BranchName.ANNA_NAGAR);
        AccountInfoDto dto1 = new AccountInfoDto(1L, "Ravi Kumar", 1000000001L, AccountType.SAVINGS, new BigDecimal("1000"), IFSC.DEMO0001001, BranchName.ANNA_NAGAR, District.CHENNAI, AccountStatus.ACTIVE, LocalDate.now(), "Arjun", "arjun@gmail.com");

        when(executiveRepository.findByUsername("admin")).thenReturn(null);
        when(branchRepository.findByName(BranchName.ANNA_NAGAR)).thenReturn(branch);
        when(accountRepository.dynamicAccountFilter(1L, null, null, null, null, PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(dto1), PageRequest.of(0, 5), 1));

        assertEquals(1, accountService.dynamicAccountFilter(BranchName.ANNA_NAGAR, null, null, null, null, 0, 5, "admin").list().size());

        verify(roleCheckUtility, times(1)).checkAdminOrExecutive("admin");
        verify(executiveRepository, times(1)).findByUsername("admin");
        verify(branchRepository, times(1)).findByName(BranchName.ANNA_NAGAR);
        verify(accountRepository, times(1)).dynamicAccountFilter(1L, null, null, null, null, PageRequest.of(0, 5));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void dynamicAccountFilterTest_AllBranches() {
        // admin without a branch filter, so accounts of all branches are listed
        AccountInfoDto dto1 = new AccountInfoDto(1L, "Ravi Kumar", 1000000001L, AccountType.SAVINGS, new BigDecimal("1000"), IFSC.DEMO0001001, BranchName.ANNA_NAGAR, District.CHENNAI, AccountStatus.ACTIVE, LocalDate.now(), "Arjun", "arjun@gmail.com");
        AccountInfoDto dto2 = new AccountInfoDto(2L, "Priya Sundaram", 1000000002L, AccountType.SAVINGS, new BigDecimal("2500"), IFSC.DEMO0001002, BranchName.T_NAGAR, District.CHENNAI, AccountStatus.ACTIVE, LocalDate.now(), "Arjun", "arjun@gmail.com");

        when(executiveRepository.findByUsername("admin")).thenReturn(null);
        when(accountRepository.dynamicAccountFilter(null, null, AccountType.SAVINGS, null, null, PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(dto1, dto2), PageRequest.of(0, 5), 2));

        assertEquals(2, accountService.dynamicAccountFilter(null, null, AccountType.SAVINGS, null, null, 0, 5, "admin").list().size());

        verify(roleCheckUtility, times(1)).checkAdminOrExecutive("admin");
        verify(executiveRepository, times(1)).findByUsername("admin");
        verify(accountRepository, times(1)).dynamicAccountFilter(null, null, AccountType.SAVINGS, null, null, PageRequest.of(0, 5));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void dynamicAccountFilterTest_Invalid() {
        // branch chosen in the filter is not in db
        when(executiveRepository.findByUsername("admin")).thenReturn(null);
        when(branchRepository.findByName(BranchName.SAKET)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> accountService.dynamicAccountFilter(BranchName.SAKET, null, null, null, null, 0, 5, "admin"));

        verify(roleCheckUtility, times(1)).checkAdminOrExecutive("admin");
        verify(executiveRepository, times(1)).findByUsername("admin");
        verify(branchRepository, times(1)).findByName(BranchName.SAKET);
        verify(accountRepository, times(0)).dynamicAccountFilter(null, null, null, null, null, PageRequest.of(0, 5));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void getAccountsByTypeTest_Executive() {
        // executive sees only the accounts of the own branch
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setName(BranchName.ANNA_NAGAR);
        Executive executive = new Executive();
        executive.setId(1L);
        executive.setBranch(branch);

        when(executiveRepository.findByUsername("arjun")).thenReturn(executive);
        when(accountRepository.getAccountCountByType(1L, AccountType.SAVINGS)).thenReturn(5L);
        when(accountRepository.getAccountCountByType(1L, AccountType.CHECKING)).thenReturn(3L);
        when(accountRepository.getAccountCountByType(1L, AccountType.BUSINESS)).thenReturn(2L);

        StatDto statDto = accountService.getAccountsByType("arjun");
        assertEquals(List.of(5L, 3L, 2L), statDto.values());

        verify(roleCheckUtility, times(1)).checkAdminOrExecutive("arjun");
        verify(executiveRepository, times(1)).findByUsername("arjun");
        verify(accountRepository, times(1)).getAccountCountByType(1L, AccountType.SAVINGS);
        verify(accountRepository, times(1)).getAccountCountByType(1L, AccountType.CHECKING);
        verify(accountRepository, times(1)).getAccountCountByType(1L, AccountType.BUSINESS);

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void getAccountsByTypeTest_Admin() {
        // admin is not an executive, so branch is null and all accounts are counted
        when(executiveRepository.findByUsername("admin")).thenReturn(null);
        when(accountRepository.getAccountCountByType(null, AccountType.SAVINGS)).thenReturn(10L);
        when(accountRepository.getAccountCountByType(null, AccountType.CHECKING)).thenReturn(6L);
        when(accountRepository.getAccountCountByType(null, AccountType.BUSINESS)).thenReturn(4L);

        StatDto statDto = accountService.getAccountsByType("admin");
        assertEquals(List.of(10L, 6L, 4L), statDto.values());

        verify(roleCheckUtility, times(1)).checkAdminOrExecutive("admin");
        verify(executiveRepository, times(1)).findByUsername("admin");
        verify(accountRepository, times(1)).getAccountCountByType(null, AccountType.SAVINGS);
        verify(accountRepository, times(1)).getAccountCountByType(null, AccountType.CHECKING);
        verify(accountRepository, times(1)).getAccountCountByType(null, AccountType.BUSINESS);

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void getAccountsByBranchTest() {
        Branch branch1 = new Branch();
        branch1.setId(1L);
        branch1.setName(BranchName.ANNA_NAGAR);
        Branch branch2 = new Branch();
        branch2.setId(2L);
        branch2.setName(BranchName.T_NAGAR);

        when(branchRepository.findAll()).thenReturn(List.of(branch1, branch2));
        when(accountRepository.getAccountCountByBranch(1L)).thenReturn(7L);
        when(accountRepository.getAccountCountByBranch(2L)).thenReturn(3L);

        StatDto statDto = accountService.getAccountsByBranch("admin");
        assertEquals(List.of(7L, 3L), statDto.values());

        verify(roleCheckUtility, times(1)).checkAdmin("admin");
        verify(branchRepository, times(1)).findAll();
        verify(accountRepository, times(1)).getAccountCountByBranch(1L);
        verify(accountRepository, times(1)).getAccountCountByBranch(2L);

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(accountRepository);
    }
}
