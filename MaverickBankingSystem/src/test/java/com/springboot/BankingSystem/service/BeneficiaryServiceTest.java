package com.springboot.BankingSystem.service;

import com.springboot.BankingSystem.dto.request.BeneficiaryDto;
import com.springboot.BankingSystem.dto.request.BeneficiaryTransactionDto;
import com.springboot.BankingSystem.dto.request.TransactionDto;
import com.springboot.BankingSystem.dto.response.BeneficiaryInfoDto;
import com.springboot.BankingSystem.enums.AccountStatus;
import com.springboot.BankingSystem.enums.BranchName;
import com.springboot.BankingSystem.enums.IFSC;
import com.springboot.BankingSystem.exception.DuplicateRequestException;
import com.springboot.BankingSystem.exception.InvalidAccountStateException;
import com.springboot.BankingSystem.exception.OwnershipException;
import com.springboot.BankingSystem.exception.ResourceNotFoundException;
import com.springboot.BankingSystem.model.Account;
import com.springboot.BankingSystem.model.Beneficiaries;
import com.springboot.BankingSystem.model.Customer;
import com.springboot.BankingSystem.model.User;
import com.springboot.BankingSystem.repository.AccountRepository;
import com.springboot.BankingSystem.repository.BeneficiaryRepository;
import com.springboot.BankingSystem.repository.CustomerRepository;
import com.springboot.BankingSystem.utility.RoleCheckUtility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BeneficiaryServiceTest {
    @InjectMocks
    private BeneficiaryService beneficiaryService;
    @Mock
    private RoleCheckUtility roleCheckUtility;
    @Mock
    private BeneficiaryRepository beneficiaryRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionService transactionService;

    @Test
    public void addTest() {
        // beneficiaryDto comes from postman/react app via controller. here, I define it myself
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto("Priya Sundaram", 1002L, BranchName.T_NAGAR, IFSC.DEMO0001002);
        // account of the beneficiary
        Customer accountHolder = new Customer();
        accountHolder.setId(2L);
        accountHolder.setName("Priya Sundaram");
        Account account = new Account();
        account.setId(2L);
        account.setAccountNumber(1002L);
        account.setStatus(AccountStatus.ACTIVE);
        account.setCustomer(accountHolder);
        // logged in customer
        Customer customer = new Customer();
        customer.setId(1L);

        when(accountRepository.findByAccountNumber(1002L)).thenReturn(account);
        when(customerRepository.findByUsername("ravi")).thenReturn(customer);
        when(beneficiaryRepository.isExists(1L, 1002L)).thenReturn(false);

        beneficiaryService.add(beneficiaryDto, "ravi");

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(accountRepository, times(1)).findByAccountNumber(1002L);
        verify(customerRepository, times(1)).findByUsername("ravi");
        verify(beneficiaryRepository, times(1)).isExists(1L, 1002L);
        verify(beneficiaryRepository, times(1)).save(any(Beneficiaries.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(beneficiaryRepository);
    }

    @Test
    public void addTest_Invalid() {
        // account number is not found
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto("Priya Sundaram", 9999L, BranchName.T_NAGAR, IFSC.DEMO0001002);

        when(accountRepository.findByAccountNumber(9999L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> beneficiaryService.add(beneficiaryDto, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(accountRepository, times(1)).findByAccountNumber(9999L);
        verify(customerRepository, times(0)).findByUsername("ravi");
        verify(beneficiaryRepository, times(0)).save(any(Beneficiaries.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(beneficiaryRepository);
    }

    @Test
    public void addTest_InactiveAccount() {
        // account of the beneficiary is closed
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto("Priya Sundaram", 1002L, BranchName.T_NAGAR, IFSC.DEMO0001002);
        Account account = new Account();
        account.setId(2L);
        account.setAccountNumber(1002L);
        account.setStatus(AccountStatus.CLOSED);

        when(accountRepository.findByAccountNumber(1002L)).thenReturn(account);

        assertThrows(InvalidAccountStateException.class, () -> beneficiaryService.add(beneficiaryDto, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(accountRepository, times(1)).findByAccountNumber(1002L);
        verify(customerRepository, times(0)).findByUsername("ravi");
        verify(beneficiaryRepository, times(0)).save(any(Beneficiaries.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(beneficiaryRepository);
    }

    @Test
    public void addTest_NameMismatch() {
        // name typed by the customer is not the account holder name
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto("Priya", 1002L, BranchName.T_NAGAR, IFSC.DEMO0001002);
        Customer accountHolder = new Customer();
        accountHolder.setId(2L);
        accountHolder.setName("Priya Sundaram");
        Account account = new Account();
        account.setId(2L);
        account.setAccountNumber(1002L);
        account.setStatus(AccountStatus.ACTIVE);
        account.setCustomer(accountHolder);

        when(accountRepository.findByAccountNumber(1002L)).thenReturn(account);

        assertThrows(IllegalArgumentException.class, () -> beneficiaryService.add(beneficiaryDto, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(accountRepository, times(1)).findByAccountNumber(1002L);
        verify(customerRepository, times(0)).findByUsername("ravi");
        verify(beneficiaryRepository, times(0)).save(any(Beneficiaries.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(beneficiaryRepository);
    }

    @Test
    public void addTest_Duplicate() {
        // beneficiary is already added by ravi
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto("Priya Sundaram", 1002L, BranchName.T_NAGAR, IFSC.DEMO0001002);
        Customer accountHolder = new Customer();
        accountHolder.setId(2L);
        accountHolder.setName("Priya Sundaram");
        Account account = new Account();
        account.setId(2L);
        account.setAccountNumber(1002L);
        account.setStatus(AccountStatus.ACTIVE);
        account.setCustomer(accountHolder);
        Customer customer = new Customer();
        customer.setId(1L);

        when(accountRepository.findByAccountNumber(1002L)).thenReturn(account);
        when(customerRepository.findByUsername("ravi")).thenReturn(customer);
        when(beneficiaryRepository.isExists(1L, 1002L)).thenReturn(true);

        assertThrows(DuplicateRequestException.class, () -> beneficiaryService.add(beneficiaryDto, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(accountRepository, times(1)).findByAccountNumber(1002L);
        verify(customerRepository, times(1)).findByUsername("ravi");
        verify(beneficiaryRepository, times(1)).isExists(1L, 1002L);
        verify(beneficiaryRepository, times(0)).save(any(Beneficiaries.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(beneficiaryRepository);
    }

    @Test
    public void addTest_NotCustomer() {
        BeneficiaryDto beneficiaryDto = new BeneficiaryDto("Priya Sundaram", 1002L, BranchName.T_NAGAR, IFSC.DEMO0001002);

        when(roleCheckUtility.checkCustomer("admin")).thenThrow(OwnershipException.class);

        assertThrows(OwnershipException.class, () -> beneficiaryService.add(beneficiaryDto, "admin"));

        verify(roleCheckUtility, times(1)).checkCustomer("admin");
        verify(accountRepository, times(0)).findByAccountNumber(1002L);
        verify(beneficiaryRepository, times(0)).save(any(Beneficiaries.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(beneficiaryRepository);
    }

    @Test
    public void deleteTest() {
        User user = new User();
        user.setUsername("ravi");
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Beneficiaries beneficiary = new Beneficiaries();
        beneficiary.setId(1L);
        beneficiary.setCustomer(customer);

        when(beneficiaryRepository.findById(1L)).thenReturn(Optional.of(beneficiary));

        beneficiaryService.delete(1L, "ravi");

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(beneficiaryRepository, times(1)).findById(1L);
        verify(beneficiaryRepository, times(1)).delete(beneficiary);

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(beneficiaryRepository);
    }

    @Test
    public void deleteTest_Invalid() {
        when(beneficiaryRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> beneficiaryService.delete(5L, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(beneficiaryRepository, times(1)).findById(5L);
        verify(beneficiaryRepository, times(0)).delete(any(Beneficiaries.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(beneficiaryRepository);
    }

    @Test
    public void deleteTest_NotOwner() {
        // the beneficiary was added by priya, but ravi is logged in
        User user = new User();
        user.setUsername("priya");
        Customer customer = new Customer();
        customer.setId(2L);
        customer.setUser(user);
        Beneficiaries beneficiary = new Beneficiaries();
        beneficiary.setId(1L);
        beneficiary.setCustomer(customer);

        when(beneficiaryRepository.findById(1L)).thenReturn(Optional.of(beneficiary));

        assertThrows(OwnershipException.class, () -> beneficiaryService.delete(1L, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(beneficiaryRepository, times(1)).findById(1L);
        verify(beneficiaryRepository, times(0)).delete(beneficiary);

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(beneficiaryRepository);
    }

    @Test
    public void getTest() {
        BeneficiaryInfoDto dto1 = new BeneficiaryInfoDto(1L, "Priya Sundaram", 1002L, BranchName.T_NAGAR, IFSC.DEMO0001002, Instant.now());
        BeneficiaryInfoDto dto2 = new BeneficiaryInfoDto(2L, "Arun Prakash", 1003L, BranchName.ANNA_NAGAR, IFSC.DEMO0001001, Instant.now());

        when(beneficiaryRepository.getByUsername("ravi")).thenReturn(List.of(dto1, dto2));

        assertEquals(2, beneficiaryService.get("ravi").size());

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(beneficiaryRepository, times(1)).getByUsername("ravi");

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(beneficiaryRepository);
    }

    @Test
    public void transferTest() {
        BeneficiaryTransactionDto beneficiaryTransactionDto = new BeneficiaryTransactionDto(new BigDecimal("500"), "1234", "Rent");
        User user = new User();
        user.setUsername("ravi");
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Beneficiaries beneficiary = new Beneficiaries();
        beneficiary.setId(2L);
        beneficiary.setAccountNumber(1002L);
        beneficiary.setCustomer(customer);
        Account account = new Account();
        account.setId(1L);
        account.setCustomer(customer);

        when(beneficiaryRepository.findById(2L)).thenReturn(Optional.of(beneficiary));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        beneficiaryService.transfer(1L, 2L, beneficiaryTransactionDto, "ravi");

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(beneficiaryRepository, times(1)).findById(2L);
        verify(accountRepository, times(1)).findById(1L);
        // beneficiary details are converted into a normal transaction and sent to the transaction service
        verify(transactionService, times(1)).add(1L, new TransactionDto(new BigDecimal("500"), 1002L, "1234", "Rent"), "ravi");

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(beneficiaryRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionService);
    }

    @Test
    public void transferTest_Invalid() {
        // beneficiary is not found
        BeneficiaryTransactionDto beneficiaryTransactionDto = new BeneficiaryTransactionDto(new BigDecimal("500"), "1234", "Rent");

        when(beneficiaryRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> beneficiaryService.transfer(1L, 5L, beneficiaryTransactionDto, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(beneficiaryRepository, times(1)).findById(5L);
        verify(accountRepository, times(0)).findById(1L);
        verify(transactionService, times(0)).add(1L, new TransactionDto(new BigDecimal("500"), 1002L, "1234", "Rent"), "ravi");

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(beneficiaryRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionService);
    }

    @Test
    public void transferTest_BeneficiaryNotOwner() {
        // the beneficiary was added by priya, but ravi is logged in
        BeneficiaryTransactionDto beneficiaryTransactionDto = new BeneficiaryTransactionDto(new BigDecimal("500"), "1234", "Rent");
        User user = new User();
        user.setUsername("priya");
        Customer customer = new Customer();
        customer.setId(2L);
        customer.setUser(user);
        Beneficiaries beneficiary = new Beneficiaries();
        beneficiary.setId(2L);
        beneficiary.setAccountNumber(1002L);
        beneficiary.setCustomer(customer);

        when(beneficiaryRepository.findById(2L)).thenReturn(Optional.of(beneficiary));

        assertThrows(OwnershipException.class, () -> beneficiaryService.transfer(1L, 2L, beneficiaryTransactionDto, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(beneficiaryRepository, times(1)).findById(2L);
        verify(accountRepository, times(0)).findById(1L);
        verify(transactionService, times(0)).add(1L, new TransactionDto(new BigDecimal("500"), 1002L, "1234", "Rent"), "ravi");

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(beneficiaryRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionService);
    }

    @Test
    public void transferTest_AccountNotFound() {
        BeneficiaryTransactionDto beneficiaryTransactionDto = new BeneficiaryTransactionDto(new BigDecimal("500"), "1234", "Rent");
        User user = new User();
        user.setUsername("ravi");
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Beneficiaries beneficiary = new Beneficiaries();
        beneficiary.setId(2L);
        beneficiary.setAccountNumber(1002L);
        beneficiary.setCustomer(customer);

        when(beneficiaryRepository.findById(2L)).thenReturn(Optional.of(beneficiary));
        when(accountRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> beneficiaryService.transfer(5L, 2L, beneficiaryTransactionDto, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(beneficiaryRepository, times(1)).findById(2L);
        verify(accountRepository, times(1)).findById(5L);
        verify(transactionService, times(0)).add(5L, new TransactionDto(new BigDecimal("500"), 1002L, "1234", "Rent"), "ravi");

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(beneficiaryRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionService);
    }

    @Test
    public void transferTest_AccountNotOwner() {
        // the beneficiary is ravi's, but the account belongs to priya
        BeneficiaryTransactionDto beneficiaryTransactionDto = new BeneficiaryTransactionDto(new BigDecimal("500"), "1234", "Rent");
        User user = new User();
        user.setUsername("ravi");
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Beneficiaries beneficiary = new Beneficiaries();
        beneficiary.setId(2L);
        beneficiary.setAccountNumber(1002L);
        beneficiary.setCustomer(customer);
        User otherUser = new User();
        otherUser.setUsername("priya");
        Customer otherCustomer = new Customer();
        otherCustomer.setId(2L);
        otherCustomer.setUser(otherUser);
        Account account = new Account();
        account.setId(3L);
        account.setCustomer(otherCustomer);

        when(beneficiaryRepository.findById(2L)).thenReturn(Optional.of(beneficiary));
        when(accountRepository.findById(3L)).thenReturn(Optional.of(account));

        assertThrows(OwnershipException.class, () -> beneficiaryService.transfer(3L, 2L, beneficiaryTransactionDto, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(beneficiaryRepository, times(1)).findById(2L);
        verify(accountRepository, times(1)).findById(3L);
        verify(transactionService, times(0)).add(3L, new TransactionDto(new BigDecimal("500"), 1002L, "1234", "Rent"), "ravi");

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(beneficiaryRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(transactionService);
    }
}
