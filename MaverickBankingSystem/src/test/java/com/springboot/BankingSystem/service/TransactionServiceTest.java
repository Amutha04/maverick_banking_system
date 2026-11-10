package com.springboot.BankingSystem.service;

import com.springboot.BankingSystem.dto.request.TransactionDto;
import com.springboot.BankingSystem.dto.response.TransactionInfoDto;
import com.springboot.BankingSystem.enums.AccountStatus;
import com.springboot.BankingSystem.enums.BranchName;
import com.springboot.BankingSystem.enums.TransactionStatus;
import com.springboot.BankingSystem.enums.TransactionType;
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
public class TransactionServiceTest {
    @InjectMocks
    private TransactionService transactionService;
    @Mock
    private RoleCheckUtility roleCheckUtility;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ExecutiveRepository executiveRepository;
    @Mock
    private BranchRepository branchRepository;

    @Test
    public void addTest() {
        // transactionDto comes from postman/react app via controller. here, I define it myself
        TransactionDto transactionDto = new TransactionDto(new BigDecimal("500"), 1002L, "1234", "Rent");
        User user = new User();
        user.setUsername("ravi");
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Account sender = new Account();
        sender.setId(1L);
        sender.setAccountNumber(1001L);
        sender.setBalance(new BigDecimal("2000"));
        sender.setPassword("encodedPassword");
        sender.setStatus(AccountStatus.ACTIVE);
        sender.setCustomer(customer);
        Account receiver = new Account();
        receiver.setId(2L);
        receiver.setAccountNumber(1002L);
        receiver.setBalance(new BigDecimal("1000"));
        receiver.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(passwordEncoder.matches("1234", "encodedPassword")).thenReturn(true);
        when(accountRepository.findByAccountNumber(1002L)).thenReturn(receiver);

        transactionService.add(1L, transactionDto, "ravi");

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(accountRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).matches("1234", "encodedPassword");
        verify(accountRepository, times(1)).findByAccountNumber(1002L);
        verify(accountRepository, times(1)).save(sender);
        verify(accountRepository, times(1)).save(receiver);
        // one debit entry for the sender and one credit entry for the receiver
        verify(transactionRepository, times(2)).save(any(Transaction.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    public void addTest_Invalid() {
        // sender account is not found
        TransactionDto transactionDto = new TransactionDto(new BigDecimal("500"), 1002L, "1234", "Rent");

        when(accountRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.add(5L, transactionDto, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(accountRepository, times(1)).findById(5L);
        verify(accountRepository, times(0)).findByAccountNumber(1002L);
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(transactionRepository, times(0)).save(any(Transaction.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    public void addTest_ZeroAmount() {
        TransactionDto transactionDto = new TransactionDto(BigDecimal.ZERO, 1002L, "1234", "Rent");

        assertThrows(IllegalArgumentException.class, () -> transactionService.add(1L, transactionDto, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(accountRepository, times(0)).findById(1L);
        verify(transactionRepository, times(0)).save(any(Transaction.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    public void addTest_NotOwner() {
        // the sender account belongs to priya, but ravi is logged in
        TransactionDto transactionDto = new TransactionDto(new BigDecimal("500"), 1002L, "1234", "Rent");
        User user = new User();
        user.setUsername("priya");
        Customer customer = new Customer();
        customer.setId(2L);
        customer.setUser(user);
        Account sender = new Account();
        sender.setId(1L);
        sender.setPassword("encodedPassword");
        sender.setStatus(AccountStatus.ACTIVE);
        sender.setCustomer(customer);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(sender));

        assertThrows(OwnershipException.class, () -> transactionService.add(1L, transactionDto, "ravi"));

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
    public void addTest_InactiveAccount() {
        // sender account is suspended
        TransactionDto transactionDto = new TransactionDto(new BigDecimal("500"), 1002L, "1234", "Rent");
        User user = new User();
        user.setUsername("ravi");
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Account sender = new Account();
        sender.setId(1L);
        sender.setPassword("encodedPassword");
        sender.setStatus(AccountStatus.SUSPENDED);
        sender.setCustomer(customer);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(sender));

        assertThrows(InvalidAccountStateException.class, () -> transactionService.add(1L, transactionDto, "ravi"));

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
    public void addTest_WrongPassword() {
        TransactionDto transactionDto = new TransactionDto(new BigDecimal("500"), 1002L, "1234", "Rent");
        User user = new User();
        user.setUsername("ravi");
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Account sender = new Account();
        sender.setId(1L);
        sender.setPassword("encodedPassword");
        sender.setStatus(AccountStatus.ACTIVE);
        sender.setCustomer(customer);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(passwordEncoder.matches("1234", "encodedPassword")).thenReturn(false);

        assertThrows(PasswordMismatchException.class, () -> transactionService.add(1L, transactionDto, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(accountRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).matches("1234", "encodedPassword");
        verify(accountRepository, times(0)).findByAccountNumber(1002L);
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(transactionRepository, times(0)).save(any(Transaction.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    public void addTest_ReceiverNotFound() {
        TransactionDto transactionDto = new TransactionDto(new BigDecimal("500"), 9999L, "1234", "Rent");
        User user = new User();
        user.setUsername("ravi");
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Account sender = new Account();
        sender.setId(1L);
        sender.setPassword("encodedPassword");
        sender.setStatus(AccountStatus.ACTIVE);
        sender.setCustomer(customer);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(passwordEncoder.matches("1234", "encodedPassword")).thenReturn(true);
        when(accountRepository.findByAccountNumber(9999L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> transactionService.add(1L, transactionDto, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(accountRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).matches("1234", "encodedPassword");
        verify(accountRepository, times(1)).findByAccountNumber(9999L);
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(transactionRepository, times(0)).save(any(Transaction.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    public void addTest_ReceiverInactive() {
        // receiver account is closed
        TransactionDto transactionDto = new TransactionDto(new BigDecimal("500"), 1002L, "1234", "Rent");
        User user = new User();
        user.setUsername("ravi");
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Account sender = new Account();
        sender.setId(1L);
        sender.setAccountNumber(1001L);
        sender.setBalance(new BigDecimal("2000"));
        sender.setPassword("encodedPassword");
        sender.setStatus(AccountStatus.ACTIVE);
        sender.setCustomer(customer);
        Account receiver = new Account();
        receiver.setId(2L);
        receiver.setAccountNumber(1002L);
        receiver.setStatus(AccountStatus.CLOSED);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(passwordEncoder.matches("1234", "encodedPassword")).thenReturn(true);
        when(accountRepository.findByAccountNumber(1002L)).thenReturn(receiver);

        assertThrows(InvalidAccountStateException.class, () -> transactionService.add(1L, transactionDto, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(accountRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).matches("1234", "encodedPassword");
        verify(accountRepository, times(1)).findByAccountNumber(1002L);
        // a failed transaction is still saved
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(accountRepository, times(0)).save(any(Account.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    public void addTest_SameAccount() {
        // ravi tries to send money to the same account
        TransactionDto transactionDto = new TransactionDto(new BigDecimal("500"), 1001L, "1234", "Rent");
        User user = new User();
        user.setUsername("ravi");
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Account sender = new Account();
        sender.setId(1L);
        sender.setAccountNumber(1001L);
        sender.setBalance(new BigDecimal("2000"));
        sender.setPassword("encodedPassword");
        sender.setStatus(AccountStatus.ACTIVE);
        sender.setCustomer(customer);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(passwordEncoder.matches("1234", "encodedPassword")).thenReturn(true);
        when(accountRepository.findByAccountNumber(1001L)).thenReturn(sender);

        assertThrows(InvalidAccountStateException.class, () -> transactionService.add(1L, transactionDto, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(accountRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).matches("1234", "encodedPassword");
        verify(accountRepository, times(1)).findByAccountNumber(1001L);
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(transactionRepository, times(0)).save(any(Transaction.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    public void addTest_InsufficientBalance() {
        // sender has only 100, but wants to send 500
        TransactionDto transactionDto = new TransactionDto(new BigDecimal("500"), 1002L, "1234", "Rent");
        User user = new User();
        user.setUsername("ravi");
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Account sender = new Account();
        sender.setId(1L);
        sender.setAccountNumber(1001L);
        sender.setBalance(new BigDecimal("100"));
        sender.setPassword("encodedPassword");
        sender.setStatus(AccountStatus.ACTIVE);
        sender.setCustomer(customer);
        Account receiver = new Account();
        receiver.setId(2L);
        receiver.setAccountNumber(1002L);
        receiver.setBalance(new BigDecimal("1000"));
        receiver.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(passwordEncoder.matches("1234", "encodedPassword")).thenReturn(true);
        when(accountRepository.findByAccountNumber(1002L)).thenReturn(receiver);

        assertThrows(InsufficientBalanceException.class, () -> transactionService.add(1L, transactionDto, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(accountRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).matches("1234", "encodedPassword");
        verify(accountRepository, times(1)).findByAccountNumber(1002L);
        // a failed transaction is still saved
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(accountRepository, times(0)).save(any(Account.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    public void addTest_NotCustomer() {
        TransactionDto transactionDto = new TransactionDto(new BigDecimal("500"), 1002L, "1234", "Rent");

        when(roleCheckUtility.checkCustomer("admin")).thenThrow(OwnershipException.class);

        assertThrows(OwnershipException.class, () -> transactionService.add(1L, transactionDto, "admin"));

        verify(roleCheckUtility, times(1)).checkCustomer("admin");
        verify(accountRepository, times(0)).findById(1L);
        verify(transactionRepository, times(0)).save(any(Transaction.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    public void getAllTest() {
        TransactionInfoDto dto1 = new TransactionInfoDto("TXN1A2B3C4D5E6F", TransactionType.DEBIT, TransactionStatus.SUCCESS, new BigDecimal("500"), 1001L, 1002L, new BigDecimal("1500"), LocalDate.now(), "Rent");
        TransactionInfoDto dto2 = new TransactionInfoDto("TXN1A2B3C4D5E6F", TransactionType.CREDIT, TransactionStatus.SUCCESS, new BigDecimal("500"), 1001L, 1002L, new BigDecimal("1500"), LocalDate.now(), "Rent");

        when(transactionRepository.getAll(PageRequest.of(0, 5))).thenReturn(List.of(dto1, dto2));

        assertEquals(2, transactionService.getAll(0, 5, "admin").size());

        verify(roleCheckUtility, times(1)).checkAdminOrExecutive("admin");
        verify(transactionRepository, times(1)).getAll(PageRequest.of(0, 5));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    public void getByAccountTest() {
        Account account = new Account();
        account.setId(1L);
        TransactionInfoDto dto1 = new TransactionInfoDto("TXN1A2B3C4D5E6F", TransactionType.DEBIT, TransactionStatus.SUCCESS, new BigDecimal("500"), 1001L, 1002L, new BigDecimal("1500"), LocalDate.now(), "Rent");
        TransactionInfoDto dto2 = new TransactionInfoDto("TXN7G8H9I0J1K2L", TransactionType.DEBIT, TransactionStatus.FAILED, new BigDecimal("5000"), 1001L, 1003L, new BigDecimal("1500"), LocalDate.now(), "Insufficient balance");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionRepository.getByAccount(1L, PageRequest.of(0, 5))).thenReturn(List.of(dto1, dto2));

        assertEquals(2, transactionService.getByAccount(0, 5, 1L, "admin").size());

        verify(roleCheckUtility, times(1)).checkAdminOrExecutive("admin");
        verify(accountRepository, times(1)).findById(1L);
        verify(transactionRepository, times(1)).getByAccount(1L, PageRequest.of(0, 5));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    public void getByAccountTest_Invalid() {
        when(accountRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.getByAccount(0, 5, 5L, "admin"));

        verify(roleCheckUtility, times(1)).checkAdminOrExecutive("admin");
        verify(accountRepository, times(1)).findById(5L);
        verify(transactionRepository, times(0)).getByAccount(5L, PageRequest.of(0, 5));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    public void getTest() {
        User user = new User();
        user.setUsername("ravi");
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Account account = new Account();
        account.setId(1L);
        account.setCustomer(customer);
        TransactionInfoDto dto1 = new TransactionInfoDto("TXN1A2B3C4D5E6F", TransactionType.DEBIT, TransactionStatus.SUCCESS, new BigDecimal("500"), 1001L, 1002L, new BigDecimal("1500"), LocalDate.now(), "Rent");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionRepository.getByAccount(1L, PageRequest.of(0, 5))).thenReturn(List.of(dto1));

        assertEquals(1, transactionService.get(0, 5, 1L, "ravi").size());

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(accountRepository, times(1)).findById(1L);
        verify(transactionRepository, times(1)).getByAccount(1L, PageRequest.of(0, 5));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    public void getTest_Invalid() {
        when(accountRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.get(0, 5, 5L, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(accountRepository, times(1)).findById(5L);
        verify(transactionRepository, times(0)).getByAccount(5L, PageRequest.of(0, 5));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    public void getTest_NotOwner() {
        // the account belongs to priya, but ravi is logged in
        User user = new User();
        user.setUsername("priya");
        Customer customer = new Customer();
        customer.setId(2L);
        customer.setUser(user);
        Account account = new Account();
        account.setId(2L);
        account.setCustomer(customer);

        when(accountRepository.findById(2L)).thenReturn(Optional.of(account));

        assertThrows(OwnershipException.class, () -> transactionService.get(0, 5, 2L, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(accountRepository, times(1)).findById(2L);
        verify(transactionRepository, times(0)).getByAccount(2L, PageRequest.of(0, 5));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    public void dynamicTransactionFilterTest_Executive() {
        // executive sees only the transactions of the own branch
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setName(BranchName.ANNA_NAGAR);
        Executive executive = new Executive();
        executive.setId(1L);
        executive.setBranch(branch);
        TransactionInfoDto dto1 = new TransactionInfoDto("TXN1A2B3C4D5E6F", TransactionType.DEBIT, TransactionStatus.SUCCESS, new BigDecimal("500"), 1001L, 1002L, new BigDecimal("1500"), LocalDate.now(), "Rent");

        when(executiveRepository.findByUsername("arjun")).thenReturn(executive);
        // the repository gives back a page, so wrap the list in PageImpl
        when(transactionRepository.dynamicTransactionFilter(1L, null, null, TransactionStatus.SUCCESS, null, null, PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(dto1), PageRequest.of(0, 5), 1));

        assertEquals(1, transactionService.dynamicTransactionFilter(null, null, null, TransactionStatus.SUCCESS, null, null, 0, 5, "arjun").list().size());

        verify(roleCheckUtility, times(1)).checkAdminOrExecutive("arjun");
        verify(executiveRepository, times(1)).findByUsername("arjun");
        verify(transactionRepository, times(1)).dynamicTransactionFilter(1L, null, null, TransactionStatus.SUCCESS, null, null, PageRequest.of(0, 5));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    public void dynamicTransactionFilterTest_Admin() {
        // admin is not an executive, so the branch is taken from the filter
        Branch branch = new Branch();
        branch.setId(2L);
        branch.setName(BranchName.T_NAGAR);
        TransactionInfoDto dto1 = new TransactionInfoDto("TXN1A2B3C4D5E6F", TransactionType.DEBIT, TransactionStatus.SUCCESS, new BigDecimal("500"), 1001L, 1002L, new BigDecimal("1500"), LocalDate.now(), "Rent");
        TransactionInfoDto dto2 = new TransactionInfoDto("TXN7G8H9I0J1K2L", TransactionType.DEPOSIT, TransactionStatus.SUCCESS, new BigDecimal("2000"), 1004L, null, new BigDecimal("3000"), LocalDate.now(), null);

        when(executiveRepository.findByUsername("admin")).thenReturn(null);
        when(branchRepository.findByName(BranchName.T_NAGAR)).thenReturn(branch);
        when(transactionRepository.dynamicTransactionFilter(2L, null, null, null, null, null, PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(dto1, dto2), PageRequest.of(0, 5), 2));

        assertEquals(2, transactionService.dynamicTransactionFilter(BranchName.T_NAGAR, null, null, null, null, null, 0, 5, "admin").list().size());

        verify(roleCheckUtility, times(1)).checkAdminOrExecutive("admin");
        verify(executiveRepository, times(1)).findByUsername("admin");
        verify(branchRepository, times(1)).findByName(BranchName.T_NAGAR);
        verify(transactionRepository, times(1)).dynamicTransactionFilter(2L, null, null, null, null, null, PageRequest.of(0, 5));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    public void dynamicTransactionFilterTest_Invalid() {
        // branch chosen by the admin is not in db
        when(executiveRepository.findByUsername("admin")).thenReturn(null);
        when(branchRepository.findByName(BranchName.SAKET)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> transactionService.dynamicTransactionFilter(BranchName.SAKET, null, null, null, null, null, 0, 5, "admin"));

        verify(roleCheckUtility, times(1)).checkAdminOrExecutive("admin");
        verify(executiveRepository, times(1)).findByUsername("admin");
        verify(branchRepository, times(1)).findByName(BranchName.SAKET);

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    public void getByUsernameTest() {
        TransactionInfoDto dto1 = new TransactionInfoDto("TXN1A2B3C4D5E6F", TransactionType.DEBIT, TransactionStatus.SUCCESS, new BigDecimal("500"), 1001L, 1002L, new BigDecimal("1500"), LocalDate.now(), "Rent");
        TransactionInfoDto dto2 = new TransactionInfoDto("TXN7G8H9I0J1K2L", TransactionType.DEBIT, TransactionStatus.SUCCESS, new BigDecimal("200"), 1001L, 1003L, new BigDecimal("1300"), LocalDate.now(), "Food");

        when(transactionRepository.getByUsername("ravi", 1001L, TransactionType.DEBIT, null, null, null, PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(dto1, dto2), PageRequest.of(0, 5), 2));

        assertEquals(2, transactionService.getByUsername(1001L, TransactionType.DEBIT, null, null, null, 0, 5, "ravi").list().size());

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(transactionRepository, times(1)).getByUsername("ravi", 1001L, TransactionType.DEBIT, null, null, null, PageRequest.of(0, 5));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(transactionRepository);
    }
}
