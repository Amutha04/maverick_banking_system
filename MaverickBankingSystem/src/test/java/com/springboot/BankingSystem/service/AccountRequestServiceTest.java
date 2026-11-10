package com.springboot.BankingSystem.service;

import com.springboot.BankingSystem.dto.request.AccountDto;
import com.springboot.BankingSystem.dto.request.RejectRequestDto;
import com.springboot.BankingSystem.dto.response.AccountRequestInfoDto;
import com.springboot.BankingSystem.dto.response.StatDto;
import com.springboot.BankingSystem.enums.AccountStatus;
import com.springboot.BankingSystem.enums.AccountType;
import com.springboot.BankingSystem.enums.BranchName;
import com.springboot.BankingSystem.enums.JobTitle;
import com.springboot.BankingSystem.enums.KYCStatus;
import com.springboot.BankingSystem.enums.RequestStatus;
import com.springboot.BankingSystem.enums.RequestType;
import com.springboot.BankingSystem.exception.DuplicateRequestException;
import com.springboot.BankingSystem.exception.EligibilityException;
import com.springboot.BankingSystem.exception.InActiveUserException;
import com.springboot.BankingSystem.exception.IncompleteVerificationException;
import com.springboot.BankingSystem.exception.InvalidAccountStateException;
import com.springboot.BankingSystem.exception.OwnershipException;
import com.springboot.BankingSystem.exception.ResourceNotFoundException;
import com.springboot.BankingSystem.model.Account;
import com.springboot.BankingSystem.model.AccountRequest;
import com.springboot.BankingSystem.model.Branch;
import com.springboot.BankingSystem.model.Customer;
import com.springboot.BankingSystem.model.Executive;
import com.springboot.BankingSystem.model.Transaction;
import com.springboot.BankingSystem.model.User;
import com.springboot.BankingSystem.repository.AccountRepository;
import com.springboot.BankingSystem.repository.AccountRequestRepository;
import com.springboot.BankingSystem.repository.BranchRepository;
import com.springboot.BankingSystem.repository.CustomerRepository;
import com.springboot.BankingSystem.repository.ExecutiveRepository;
import com.springboot.BankingSystem.repository.LoanRepository;
import com.springboot.BankingSystem.repository.TransactionRepository;
import com.springboot.BankingSystem.utility.RoleCheckUtility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountRequestServiceTest {
    @InjectMocks
    private AccountRequestService accountRequestService;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private AccountRequestRepository accountRequestRepository;
    @Mock
    private ExecutiveRepository executiveRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private LoanRepository loanRepository;
    @Mock
    private RoleCheckUtility roleCheckUtility;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private TransactionService transactionService;
    @Mock
    private BranchRepository branchRepository;

    @Test
    public void requestTest_Open() throws IOException {
        // accountDto comes from postman/react app via controller. here, I define it myself
        Customer customer = new Customer();
        customer.setId(1L);
        AccountDto accountDto = new AccountDto(RequestType.OPEN, AccountType.SAVINGS, 123456789012L, "ABCDE1234F", "ANNA_NAGAR", null, new BigDecimal("5000"));

        when(customerRepository.findByUsername("ravi")).thenReturn(customer);
        when(accountRequestRepository.isExistsForOpen(1L, AccountType.SAVINGS)).thenReturn(false);

        accountRequestService.request(accountDto, null, "ravi");

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(customerRepository, times(1)).findByUsername("ravi");
        verify(accountRequestRepository, times(1)).isExistsForOpen(1L, AccountType.SAVINGS);
        verify(accountRequestRepository, times(1)).save(any(AccountRequest.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRequestRepository);
    }

    @Test
    public void requestTest_Duplicate() {
        // same account type is already requested or opened
        Customer customer = new Customer();
        customer.setId(1L);
        AccountDto accountDto = new AccountDto(RequestType.OPEN, AccountType.SAVINGS, 123456789012L, "ABCDE1234F", "ANNA_NAGAR", null, new BigDecimal("5000"));

        when(customerRepository.findByUsername("ravi")).thenReturn(customer);
        when(accountRequestRepository.isExistsForOpen(1L, AccountType.SAVINGS)).thenReturn(true);

        assertThrows(DuplicateRequestException.class, () -> accountRequestService.request(accountDto, null, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(customerRepository, times(1)).findByUsername("ravi");
        verify(accountRequestRepository, times(1)).isExistsForOpen(1L, AccountType.SAVINGS);
        verify(accountRequestRepository, times(0)).save(any(AccountRequest.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRequestRepository);
    }

    @Test
    public void requestTest_Close() throws IOException {
        User user = new User();
        user.setUsername("ravi");
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setName(BranchName.ANNA_NAGAR);
        Account account = new Account();
        account.setId(1L);
        account.setStatus(AccountStatus.ACTIVE);
        account.setCustomer(customer);
        account.setBranch(branch);
        AccountDto accountDto = new AccountDto(RequestType.CLOSE, AccountType.SAVINGS, null, null, null, "Moving to another city", null);

        when(customerRepository.findByUsername("ravi")).thenReturn(customer);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        accountRequestService.request(accountDto, 1L, "ravi");

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(customerRepository, times(1)).findByUsername("ravi");
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).save(account);
        verify(accountRequestRepository, times(1)).save(any(AccountRequest.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(accountRequestRepository);
    }

    @Test
    public void requestTest_Invalid() {
        // account to close is not found
        AccountDto accountDto = new AccountDto(RequestType.CLOSE, AccountType.SAVINGS, null, null, null, "Moving to another city", null);

        when(accountRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountRequestService.request(accountDto, 5L, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(customerRepository, times(1)).findByUsername("ravi");
        verify(accountRepository, times(1)).findById(5L);
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(accountRequestRepository, times(0)).save(any(AccountRequest.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(accountRequestRepository);
    }

    @Test
    public void requestTest_NotOwner() {
        // account belongs to another customer
        User user = new User();
        user.setUsername("priya");
        Customer customer = new Customer();
        customer.setId(2L);
        customer.setUser(user);
        Account account = new Account();
        account.setId(1L);
        account.setStatus(AccountStatus.ACTIVE);
        account.setCustomer(customer);
        AccountDto accountDto = new AccountDto(RequestType.CLOSE, AccountType.SAVINGS, null, null, null, "Moving to another city", null);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThrows(OwnershipException.class, () -> accountRequestService.request(accountDto, 1L, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(customerRepository, times(1)).findByUsername("ravi");
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(accountRequestRepository, times(0)).save(any(AccountRequest.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(accountRequestRepository);
    }

    @Test
    public void requestTest_NotActive() {
        // account is already closed
        User user = new User();
        user.setUsername("ravi");
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Account account = new Account();
        account.setId(1L);
        account.setStatus(AccountStatus.CLOSED);
        account.setCustomer(customer);
        AccountDto accountDto = new AccountDto(RequestType.CLOSE, AccountType.SAVINGS, null, null, null, "Moving to another city", null);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThrows(InvalidAccountStateException.class, () -> accountRequestService.request(accountDto, 1L, "ravi"));

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(customerRepository, times(1)).findByUsername("ravi");
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(accountRequestRepository, times(0)).save(any(AccountRequest.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(accountRequestRepository);
    }

    @Test
    public void requestTest_NotCustomer() {
        AccountDto accountDto = new AccountDto(RequestType.OPEN, AccountType.SAVINGS, 123456789012L, "ABCDE1234F", "ANNA_NAGAR", null, new BigDecimal("5000"));

        when(roleCheckUtility.checkCustomer("arjun")).thenThrow(OwnershipException.class);

        assertThrows(OwnershipException.class, () -> accountRequestService.request(accountDto, null, "arjun"));

        verify(roleCheckUtility, times(1)).checkCustomer("arjun");
        verify(customerRepository, times(0)).findByUsername("arjun");
        verify(accountRequestRepository, times(0)).save(any(AccountRequest.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(customerRepository);
        verifyNoMoreInteractions(accountRequestRepository);
    }

    @Test
    public void approveOpenRequestTest() {
        User user = new User();
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setDob(LocalDate.of(1990, 5, 15));
        customer.setKycStatus(KYCStatus.DONE);
        customer.setUser(user);
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setName(BranchName.ANNA_NAGAR);
        Executive executive = new Executive();
        executive.setId(1L);
        executive.setBranch(branch);
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setId(1L);
        accountRequest.setRequestType(RequestType.OPEN);
        accountRequest.setAccountType(AccountType.SAVINGS);
        accountRequest.setBranchName("ANNA_NAGAR");
        accountRequest.setInitialDeposit(new BigDecimal("5000"));
        accountRequest.setCustomer(customer);

        when(accountRequestRepository.findById(1L)).thenReturn(Optional.of(accountRequest));
        when(executiveRepository.findByUsername("arjun")).thenReturn(executive);
        when(branchRepository.findByName(BranchName.ANNA_NAGAR)).thenReturn(branch);

        accountRequestService.approveOpenRequest(1L, "arjun");

        verify(roleCheckUtility, times(1)).checkExecutive("arjun");
        verify(accountRequestRepository, times(1)).findById(1L);
        verify(executiveRepository, times(1)).findByUsername("arjun");

        verify(accountRepository, times(1)).existsByAccountNumber(anyLong());
        verify(branchRepository, times(1)).findByName(BranchName.ANNA_NAGAR);
        verify(accountRepository, times(1)).save(any(Account.class));

        verify(transactionService, times(1)).generateReferenceNumber();
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(accountRequestRepository, times(1)).save(accountRequest);

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRequestRepository);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(transactionService);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    public void approveOpenRequestTest_NoDeposit() {
        // no initial deposit, so no transaction is saved
        User user = new User();
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setDob(LocalDate.of(1990, 5, 15));
        customer.setKycStatus(KYCStatus.DONE);
        customer.setUser(user);
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setName(BranchName.ANNA_NAGAR);
        Executive executive = new Executive();
        executive.setId(1L);
        executive.setBranch(branch);
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setId(1L);
        accountRequest.setRequestType(RequestType.OPEN);
        accountRequest.setAccountType(AccountType.SAVINGS);
        accountRequest.setBranchName("ANNA_NAGAR");
        accountRequest.setCustomer(customer);

        when(accountRequestRepository.findById(1L)).thenReturn(Optional.of(accountRequest));
        when(executiveRepository.findByUsername("arjun")).thenReturn(executive);
        when(branchRepository.findByName(BranchName.ANNA_NAGAR)).thenReturn(branch);

        accountRequestService.approveOpenRequest(1L, "arjun");

        verify(roleCheckUtility, times(1)).checkExecutive("arjun");
        verify(accountRequestRepository, times(1)).findById(1L);
        verify(executiveRepository, times(1)).findByUsername("arjun");
        verify(accountRepository, times(1)).existsByAccountNumber(anyLong());
        verify(branchRepository, times(1)).findByName(BranchName.ANNA_NAGAR);
        verify(accountRepository, times(1)).save(any(Account.class));
        verify(transactionService, times(0)).generateReferenceNumber();
        verify(transactionRepository, times(0)).save(any(Transaction.class));
        verify(accountRequestRepository, times(1)).save(accountRequest);

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRequestRepository);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(accountRepository);
        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(transactionService);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    public void approveOpenRequestTest_Invalid() {
        when(accountRequestRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountRequestService.approveOpenRequest(5L, "arjun"));

        verify(roleCheckUtility, times(1)).checkExecutive("arjun");
        verify(accountRequestRepository, times(1)).findById(5L);
        verify(executiveRepository, times(0)).findByUsername("arjun");
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(accountRequestRepository, times(0)).save(any(AccountRequest.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRequestRepository);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void approveOpenRequestTest_InactiveCustomer() {
        // soft deleted customer, so the user is not active
        User user = new User();
        user.setActive(false);
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setId(1L);
        accountRequest.setRequestType(RequestType.OPEN);
        accountRequest.setCustomer(customer);

        when(accountRequestRepository.findById(1L)).thenReturn(Optional.of(accountRequest));

        assertThrows(ResourceNotFoundException.class, () -> accountRequestService.approveOpenRequest(1L, "arjun"));

        verify(roleCheckUtility, times(1)).checkExecutive("arjun");
        verify(accountRequestRepository, times(1)).findById(1L);
        verify(executiveRepository, times(0)).findByUsername("arjun");
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(accountRequestRepository, times(0)).save(any(AccountRequest.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRequestRepository);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void approveOpenRequestTest_AlreadyProcessed() {
        User user = new User();
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setId(1L);
        accountRequest.setRequestType(RequestType.OPEN);
        accountRequest.setRequestStatus(RequestStatus.APPROVED);
        accountRequest.setCustomer(customer);

        when(accountRequestRepository.findById(1L)).thenReturn(Optional.of(accountRequest));

        assertThrows(InvalidAccountStateException.class, () -> accountRequestService.approveOpenRequest(1L, "arjun"));

        verify(roleCheckUtility, times(1)).checkExecutive("arjun");
        verify(accountRequestRepository, times(1)).findById(1L);
        verify(executiveRepository, times(0)).findByUsername("arjun");
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(accountRequestRepository, times(0)).save(any(AccountRequest.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRequestRepository);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void approveOpenRequestTest_AssignedToOther() {
        // request is assigned to another executive
        User user = new User();
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Executive assignedExecutive = new Executive();
        assignedExecutive.setId(2L);
        Executive executive = new Executive();
        executive.setId(1L);
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setId(1L);
        accountRequest.setRequestType(RequestType.OPEN);
        accountRequest.setBranchName("ANNA_NAGAR");
        accountRequest.setCustomer(customer);
        accountRequest.setExecutive(assignedExecutive);

        when(accountRequestRepository.findById(1L)).thenReturn(Optional.of(accountRequest));
        when(executiveRepository.findByUsername("arjun")).thenReturn(executive);

        assertThrows(OwnershipException.class, () -> accountRequestService.approveOpenRequest(1L, "arjun"));

        verify(roleCheckUtility, times(1)).checkExecutive("arjun");
        verify(accountRequestRepository, times(1)).findById(1L);
        verify(executiveRepository, times(1)).findByUsername("arjun");
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(accountRequestRepository, times(0)).save(any(AccountRequest.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRequestRepository);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void approveOpenRequestTest_OtherBranch() {
        // unassigned request of another branch
        User user = new User();
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setName(BranchName.ANNA_NAGAR);
        Executive executive = new Executive();
        executive.setId(1L);
        executive.setBranch(branch);
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setId(1L);
        accountRequest.setRequestType(RequestType.OPEN);
        accountRequest.setBranchName("T_NAGAR");
        accountRequest.setCustomer(customer);

        when(accountRequestRepository.findById(1L)).thenReturn(Optional.of(accountRequest));
        when(executiveRepository.findByUsername("arjun")).thenReturn(executive);

        assertThrows(OwnershipException.class, () -> accountRequestService.approveOpenRequest(1L, "arjun"));

        verify(roleCheckUtility, times(1)).checkExecutive("arjun");
        verify(accountRequestRepository, times(1)).findById(1L);
        verify(executiveRepository, times(1)).findByUsername("arjun");
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(accountRequestRepository, times(0)).save(any(AccountRequest.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRequestRepository);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void approveOpenRequestTest_KycPending() {
        User user = new User();
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setDob(LocalDate.of(1990, 5, 15));
        customer.setKycStatus(KYCStatus.PENDING);
        customer.setUser(user);
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setName(BranchName.ANNA_NAGAR);
        Executive executive = new Executive();
        executive.setId(1L);
        executive.setBranch(branch);
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setId(1L);
        accountRequest.setRequestType(RequestType.OPEN);
        accountRequest.setBranchName("ANNA_NAGAR");
        accountRequest.setCustomer(customer);

        when(accountRequestRepository.findById(1L)).thenReturn(Optional.of(accountRequest));
        when(executiveRepository.findByUsername("arjun")).thenReturn(executive);

        assertThrows(IncompleteVerificationException.class, () -> accountRequestService.approveOpenRequest(1L, "arjun"));

        verify(roleCheckUtility, times(1)).checkExecutive("arjun");
        verify(accountRequestRepository, times(1)).findById(1L);
        verify(executiveRepository, times(1)).findByUsername("arjun");
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(accountRequestRepository, times(0)).save(any(AccountRequest.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRequestRepository);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void approveOpenRequestTest_Underage() {
        // customer is only 10 years old
        User user = new User();
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setDob(LocalDate.now().minusYears(10));
        customer.setKycStatus(KYCStatus.DONE);
        customer.setUser(user);
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setName(BranchName.ANNA_NAGAR);
        Executive executive = new Executive();
        executive.setId(1L);
        executive.setBranch(branch);
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setId(1L);
        accountRequest.setRequestType(RequestType.OPEN);
        accountRequest.setBranchName("ANNA_NAGAR");
        accountRequest.setCustomer(customer);

        when(accountRequestRepository.findById(1L)).thenReturn(Optional.of(accountRequest));
        when(executiveRepository.findByUsername("arjun")).thenReturn(executive);

        assertThrows(EligibilityException.class, () -> accountRequestService.approveOpenRequest(1L, "arjun"));

        verify(roleCheckUtility, times(1)).checkExecutive("arjun");
        verify(accountRequestRepository, times(1)).findById(1L);
        verify(executiveRepository, times(1)).findByUsername("arjun");
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(accountRequestRepository, times(0)).save(any(AccountRequest.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRequestRepository);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void rejectRequestTest() {
        User user = new User();
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setName(BranchName.ANNA_NAGAR);
        Executive executive = new Executive();
        executive.setId(1L);
        executive.setBranch(branch);
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setId(1L);
        accountRequest.setRequestType(RequestType.OPEN);
        accountRequest.setBranchName("ANNA_NAGAR");
        accountRequest.setCustomer(customer);
        RejectRequestDto rejectRequestDto = new RejectRequestDto("Documents are not clear");

        when(accountRequestRepository.findById(1L)).thenReturn(Optional.of(accountRequest));
        when(executiveRepository.findByUsername("arjun")).thenReturn(executive);

        accountRequestService.rejectRequest(1L, rejectRequestDto, "arjun");

        verify(roleCheckUtility, times(1)).checkExecutive("arjun");
        verify(accountRequestRepository, times(1)).findById(1L);
        verify(executiveRepository, times(1)).findByUsername("arjun");
        // open request has no account, so no account update
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(accountRequestRepository, times(1)).save(accountRequest);

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRequestRepository);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void rejectRequestTest_Close() {
        // close request is rejected, so the account becomes active again
        User user = new User();
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setName(BranchName.ANNA_NAGAR);
        Executive executive = new Executive();
        executive.setId(1L);
        executive.setBranch(branch);
        Account account = new Account();
        account.setId(1L);
        account.setStatus(AccountStatus.CLOSURE_REQUEST);
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setId(1L);
        accountRequest.setRequestType(RequestType.CLOSE);
        accountRequest.setBranchName("ANNA_NAGAR");
        accountRequest.setCustomer(customer);
        accountRequest.setAccount(account);
        RejectRequestDto rejectRequestDto = new RejectRequestDto("Loan dues are pending");

        when(accountRequestRepository.findById(1L)).thenReturn(Optional.of(accountRequest));
        when(executiveRepository.findByUsername("arjun")).thenReturn(executive);

        accountRequestService.rejectRequest(1L, rejectRequestDto, "arjun");

        verify(roleCheckUtility, times(1)).checkExecutive("arjun");
        verify(accountRequestRepository, times(1)).findById(1L);
        verify(executiveRepository, times(1)).findByUsername("arjun");
        verify(accountRepository, times(1)).save(account);
        verify(accountRequestRepository, times(1)).save(accountRequest);

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRequestRepository);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void rejectRequestTest_BlankReason() {
        RejectRequestDto rejectRequestDto = new RejectRequestDto(" ");

        assertThrows(IllegalArgumentException.class, () -> accountRequestService.rejectRequest(1L, rejectRequestDto, "arjun"));

        verify(roleCheckUtility, times(1)).checkExecutive("arjun");
        verify(accountRequestRepository, times(0)).findById(1L);
        verify(accountRequestRepository, times(0)).save(any(AccountRequest.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRequestRepository);
    }

    @Test
    public void rejectRequestTest_Invalid() {
        RejectRequestDto rejectRequestDto = new RejectRequestDto("Documents are not clear");

        when(accountRequestRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountRequestService.rejectRequest(5L, rejectRequestDto, "arjun"));

        verify(roleCheckUtility, times(1)).checkExecutive("arjun");
        verify(accountRequestRepository, times(1)).findById(5L);
        verify(executiveRepository, times(0)).findByUsername("arjun");
        verify(accountRequestRepository, times(0)).save(any(AccountRequest.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRequestRepository);
        verifyNoMoreInteractions(executiveRepository);
    }

    @Test
    public void rejectRequestTest_InactiveCustomer() {
        // soft deleted customer, so the user is not active
        User user = new User();
        user.setActive(false);
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setId(1L);
        accountRequest.setRequestType(RequestType.OPEN);
        accountRequest.setCustomer(customer);
        RejectRequestDto rejectRequestDto = new RejectRequestDto("Documents are not clear");

        when(accountRequestRepository.findById(1L)).thenReturn(Optional.of(accountRequest));

        assertThrows(ResourceNotFoundException.class, () -> accountRequestService.rejectRequest(1L, rejectRequestDto, "arjun"));

        verify(roleCheckUtility, times(1)).checkExecutive("arjun");
        verify(accountRequestRepository, times(1)).findById(1L);
        verify(executiveRepository, times(0)).findByUsername("arjun");
        verify(accountRequestRepository, times(0)).save(any(AccountRequest.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRequestRepository);
        verifyNoMoreInteractions(executiveRepository);
    }

    @Test
    public void rejectRequestTest_AlreadyProcessed() {
        User user = new User();
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setId(1L);
        accountRequest.setRequestType(RequestType.OPEN);
        accountRequest.setRequestStatus(RequestStatus.REJECTED);
        accountRequest.setCustomer(customer);
        RejectRequestDto rejectRequestDto = new RejectRequestDto("Documents are not clear");

        when(accountRequestRepository.findById(1L)).thenReturn(Optional.of(accountRequest));

        assertThrows(InvalidAccountStateException.class, () -> accountRequestService.rejectRequest(1L, rejectRequestDto, "arjun"));

        verify(roleCheckUtility, times(1)).checkExecutive("arjun");
        verify(accountRequestRepository, times(1)).findById(1L);
        verify(executiveRepository, times(0)).findByUsername("arjun");
        verify(accountRequestRepository, times(0)).save(any(AccountRequest.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRequestRepository);
        verifyNoMoreInteractions(executiveRepository);
    }

    @Test
    public void approveCloseRequestTest() {
        User user = new User();
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setName(BranchName.ANNA_NAGAR);
        Executive executive = new Executive();
        executive.setId(1L);
        executive.setBranch(branch);
        Account account = new Account();
        account.setId(1L);
        account.setStatus(AccountStatus.CLOSURE_REQUEST);
        account.setBalance(BigDecimal.ZERO);
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setId(1L);
        accountRequest.setRequestType(RequestType.CLOSE);
        accountRequest.setBranchName("ANNA_NAGAR");
        accountRequest.setCustomer(customer);
        accountRequest.setAccount(account);

        when(accountRequestRepository.findById(1L)).thenReturn(Optional.of(accountRequest));
        when(executiveRepository.findByUsername("arjun")).thenReturn(executive);
        when(loanRepository.checkForOpenLoan(1L)).thenReturn(false);

        accountRequestService.approveCloseRequest(1L, "arjun");

        verify(roleCheckUtility, times(1)).checkExecutive("arjun");
        verify(accountRequestRepository, times(1)).findById(1L);
        verify(executiveRepository, times(1)).findByUsername("arjun");
        verify(loanRepository, times(1)).checkForOpenLoan(1L);
        verify(loanRepository, times(1)).rejectNotOpenedLoans(1L, "Account is closed");
        verify(accountRepository, times(1)).save(account);
        verify(accountRequestRepository, times(1)).save(accountRequest);

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRequestRepository);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(loanRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void approveCloseRequestTest_Invalid() {
        when(accountRequestRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountRequestService.approveCloseRequest(5L, "arjun"));

        verify(roleCheckUtility, times(1)).checkExecutive("arjun");
        verify(accountRequestRepository, times(1)).findById(5L);
        verify(executiveRepository, times(0)).findByUsername("arjun");
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(accountRequestRepository, times(0)).save(any(AccountRequest.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRequestRepository);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void approveCloseRequestTest_InactiveCustomer() {
        // soft deleted customer, so the user is not active
        User user = new User();
        user.setActive(false);
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setId(1L);
        accountRequest.setRequestType(RequestType.CLOSE);
        accountRequest.setCustomer(customer);

        when(accountRequestRepository.findById(1L)).thenReturn(Optional.of(accountRequest));

        assertThrows(ResourceNotFoundException.class, () -> accountRequestService.approveCloseRequest(1L, "arjun"));

        verify(roleCheckUtility, times(1)).checkExecutive("arjun");
        verify(accountRequestRepository, times(1)).findById(1L);
        verify(executiveRepository, times(0)).findByUsername("arjun");
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(accountRequestRepository, times(0)).save(any(AccountRequest.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRequestRepository);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void approveCloseRequestTest_AlreadyProcessed() {
        User user = new User();
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setId(1L);
        accountRequest.setRequestType(RequestType.CLOSE);
        accountRequest.setRequestStatus(RequestStatus.APPROVED);
        accountRequest.setCustomer(customer);

        when(accountRequestRepository.findById(1L)).thenReturn(Optional.of(accountRequest));

        assertThrows(InvalidAccountStateException.class, () -> accountRequestService.approveCloseRequest(1L, "arjun"));

        verify(roleCheckUtility, times(1)).checkExecutive("arjun");
        verify(accountRequestRepository, times(1)).findById(1L);
        verify(executiveRepository, times(0)).findByUsername("arjun");
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(accountRequestRepository, times(0)).save(any(AccountRequest.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRequestRepository);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void approveCloseRequestTest_NotClosureRequest() {
        // account is still active, not in closure request
        User user = new User();
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setName(BranchName.ANNA_NAGAR);
        Executive executive = new Executive();
        executive.setId(1L);
        executive.setBranch(branch);
        Account account = new Account();
        account.setId(1L);
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(BigDecimal.ZERO);
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setId(1L);
        accountRequest.setRequestType(RequestType.CLOSE);
        accountRequest.setBranchName("ANNA_NAGAR");
        accountRequest.setCustomer(customer);
        accountRequest.setAccount(account);

        when(accountRequestRepository.findById(1L)).thenReturn(Optional.of(accountRequest));
        when(executiveRepository.findByUsername("arjun")).thenReturn(executive);

        assertThrows(InvalidAccountStateException.class, () -> accountRequestService.approveCloseRequest(1L, "arjun"));

        verify(roleCheckUtility, times(1)).checkExecutive("arjun");
        verify(accountRequestRepository, times(1)).findById(1L);
        verify(executiveRepository, times(1)).findByUsername("arjun");
        verify(loanRepository, times(0)).checkForOpenLoan(1L);
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(accountRequestRepository, times(0)).save(any(AccountRequest.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRequestRepository);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(loanRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void approveCloseRequestTest_BalanceNotZero() {
        // balance must be withdrawn before closing
        User user = new User();
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setName(BranchName.ANNA_NAGAR);
        Executive executive = new Executive();
        executive.setId(1L);
        executive.setBranch(branch);
        Account account = new Account();
        account.setId(1L);
        account.setStatus(AccountStatus.CLOSURE_REQUEST);
        account.setBalance(new BigDecimal("1500"));
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setId(1L);
        accountRequest.setRequestType(RequestType.CLOSE);
        accountRequest.setBranchName("ANNA_NAGAR");
        accountRequest.setCustomer(customer);
        accountRequest.setAccount(account);

        when(accountRequestRepository.findById(1L)).thenReturn(Optional.of(accountRequest));
        when(executiveRepository.findByUsername("arjun")).thenReturn(executive);

        assertThrows(InvalidAccountStateException.class, () -> accountRequestService.approveCloseRequest(1L, "arjun"));

        verify(roleCheckUtility, times(1)).checkExecutive("arjun");
        verify(accountRequestRepository, times(1)).findById(1L);
        verify(executiveRepository, times(1)).findByUsername("arjun");
        verify(loanRepository, times(0)).checkForOpenLoan(1L);
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(accountRequestRepository, times(0)).save(any(AccountRequest.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRequestRepository);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(loanRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void approveCloseRequestTest_OpenLoans() {
        User user = new User();
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setName(BranchName.ANNA_NAGAR);
        Executive executive = new Executive();
        executive.setId(1L);
        executive.setBranch(branch);
        Account account = new Account();
        account.setId(1L);
        account.setStatus(AccountStatus.CLOSURE_REQUEST);
        account.setBalance(BigDecimal.ZERO);
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setId(1L);
        accountRequest.setRequestType(RequestType.CLOSE);
        accountRequest.setBranchName("ANNA_NAGAR");
        accountRequest.setCustomer(customer);
        accountRequest.setAccount(account);

        when(accountRequestRepository.findById(1L)).thenReturn(Optional.of(accountRequest));
        when(executiveRepository.findByUsername("arjun")).thenReturn(executive);
        when(loanRepository.checkForOpenLoan(1L)).thenReturn(true);

        assertThrows(InvalidAccountStateException.class, () -> accountRequestService.approveCloseRequest(1L, "arjun"));

        verify(roleCheckUtility, times(1)).checkExecutive("arjun");
        verify(accountRequestRepository, times(1)).findById(1L);
        verify(executiveRepository, times(1)).findByUsername("arjun");
        verify(loanRepository, times(1)).checkForOpenLoan(1L);
        verify(loanRepository, times(0)).rejectNotOpenedLoans(1L, "Account is closed");
        verify(accountRepository, times(0)).save(any(Account.class));
        verify(accountRequestRepository, times(0)).save(any(AccountRequest.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRequestRepository);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(loanRepository);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void getByUsernameTest() {
        AccountRequestInfoDto dto1 = new AccountRequestInfoDto(1L, RequestType.OPEN, AccountType.SAVINGS, RequestStatus.IN_PROCESS, "ANNA_NAGAR", null, new BigDecimal("5000"), LocalDate.now(), null, null, 1L, "Ravi Kumar", null, null);
        AccountRequestInfoDto dto2 = new AccountRequestInfoDto(2L, RequestType.CLOSE, AccountType.CHECKING, RequestStatus.IN_PROCESS, "ANNA_NAGAR", "Moving to another city", null, LocalDate.now(), null, null, 1L, "Ravi Kumar", "Arjun", "arjun@gmail.com");

        // the repository gives back a page, so wrap the list in PageImpl
        when(accountRequestRepository.getByUsername("ravi", null, RequestStatus.IN_PROCESS, null, null, PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(dto1, dto2), PageRequest.of(0, 5), 2));

        assertEquals(2, accountRequestService.getByUsername(null, RequestStatus.IN_PROCESS, null, null, 0, 5, "ravi").list().size());

        verify(roleCheckUtility, times(1)).checkCustomer("ravi");
        verify(accountRequestRepository, times(1)).getByUsername("ravi", null, RequestStatus.IN_PROCESS, null, null, PageRequest.of(0, 5));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRequestRepository);
    }

    @Test
    public void assignExecutiveTest() {
        User user = new User();
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setName(BranchName.ANNA_NAGAR);
        Executive manager = new Executive();
        manager.setId(1L);
        manager.setBranch(branch);
        User executiveUser = new User();
        Executive executive = new Executive();
        executive.setId(2L);
        executive.setUser(executiveUser);
        executive.setBranch(branch);
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setId(1L);
        accountRequest.setRequestType(RequestType.OPEN);
        accountRequest.setBranchName("ANNA_NAGAR");
        accountRequest.setCustomer(customer);

        when(accountRequestRepository.findById(1L)).thenReturn(Optional.of(accountRequest));
        when(executiveRepository.findByUsername("meena")).thenReturn(manager);
        when(executiveRepository.findById(2L)).thenReturn(Optional.of(executive));

        accountRequestService.assignExecutive(1L, 2L, "meena");

        verify(roleCheckUtility, times(1)).checkManager("meena");
        verify(accountRequestRepository, times(1)).findById(1L);
        verify(executiveRepository, times(1)).findByUsername("meena");
        verify(executiveRepository, times(1)).findById(2L);
        verify(accountRequestRepository, times(1)).save(accountRequest);

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRequestRepository);
        verifyNoMoreInteractions(executiveRepository);
    }

    @Test
    public void assignExecutiveTest_Invalid() {
        when(accountRequestRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountRequestService.assignExecutive(5L, 2L, "meena"));

        verify(roleCheckUtility, times(1)).checkManager("meena");
        verify(accountRequestRepository, times(1)).findById(5L);
        verify(executiveRepository, times(0)).findById(2L);
        verify(accountRequestRepository, times(0)).save(any(AccountRequest.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRequestRepository);
        verifyNoMoreInteractions(executiveRepository);
    }

    @Test
    public void assignExecutiveTest_InactiveCustomer() {
        // soft deleted customer, so the user is not active
        User user = new User();
        user.setActive(false);
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setId(1L);
        accountRequest.setRequestType(RequestType.OPEN);
        accountRequest.setCustomer(customer);

        when(accountRequestRepository.findById(1L)).thenReturn(Optional.of(accountRequest));

        assertThrows(ResourceNotFoundException.class, () -> accountRequestService.assignExecutive(1L, 2L, "meena"));

        verify(roleCheckUtility, times(1)).checkManager("meena");
        verify(accountRequestRepository, times(1)).findById(1L);
        verify(executiveRepository, times(0)).findById(2L);
        verify(accountRequestRepository, times(0)).save(any(AccountRequest.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRequestRepository);
        verifyNoMoreInteractions(executiveRepository);
    }

    @Test
    public void assignExecutiveTest_AlreadyProcessed() {
        User user = new User();
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setId(1L);
        accountRequest.setRequestType(RequestType.OPEN);
        accountRequest.setRequestStatus(RequestStatus.APPROVED);
        accountRequest.setCustomer(customer);

        when(accountRequestRepository.findById(1L)).thenReturn(Optional.of(accountRequest));

        assertThrows(InvalidAccountStateException.class, () -> accountRequestService.assignExecutive(1L, 2L, "meena"));

        verify(roleCheckUtility, times(1)).checkManager("meena");
        verify(accountRequestRepository, times(1)).findById(1L);
        verify(executiveRepository, times(0)).findById(2L);
        verify(accountRequestRepository, times(0)).save(any(AccountRequest.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRequestRepository);
        verifyNoMoreInteractions(executiveRepository);
    }

    @Test
    public void assignExecutiveTest_AlreadyAssigned() {
        User user = new User();
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Executive assignedExecutive = new Executive();
        assignedExecutive.setId(3L);
        assignedExecutive.setName("Arjun");
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setId(1L);
        accountRequest.setRequestType(RequestType.OPEN);
        accountRequest.setBranchName("ANNA_NAGAR");
        accountRequest.setCustomer(customer);
        accountRequest.setExecutive(assignedExecutive);

        when(accountRequestRepository.findById(1L)).thenReturn(Optional.of(accountRequest));

        assertThrows(InvalidAccountStateException.class, () -> accountRequestService.assignExecutive(1L, 2L, "meena"));

        verify(roleCheckUtility, times(1)).checkManager("meena");
        verify(accountRequestRepository, times(1)).findById(1L);
        verify(executiveRepository, times(0)).findByUsername("meena");
        verify(executiveRepository, times(0)).findById(2L);
        verify(accountRequestRepository, times(0)).save(any(AccountRequest.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRequestRepository);
        verifyNoMoreInteractions(executiveRepository);
    }

    @Test
    public void assignExecutiveTest_OtherBranch() {
        // request belongs to another branch, not the manager's branch
        User user = new User();
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setName(BranchName.ANNA_NAGAR);
        Executive manager = new Executive();
        manager.setId(1L);
        manager.setBranch(branch);
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setId(1L);
        accountRequest.setRequestType(RequestType.OPEN);
        accountRequest.setBranchName("T_NAGAR");
        accountRequest.setCustomer(customer);

        when(accountRequestRepository.findById(1L)).thenReturn(Optional.of(accountRequest));
        when(executiveRepository.findByUsername("meena")).thenReturn(manager);

        assertThrows(OwnershipException.class, () -> accountRequestService.assignExecutive(1L, 2L, "meena"));

        verify(roleCheckUtility, times(1)).checkManager("meena");
        verify(accountRequestRepository, times(1)).findById(1L);
        verify(executiveRepository, times(1)).findByUsername("meena");
        verify(executiveRepository, times(0)).findById(2L);
        verify(accountRequestRepository, times(0)).save(any(AccountRequest.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRequestRepository);
        verifyNoMoreInteractions(executiveRepository);
    }

    @Test
    public void assignExecutiveTest_ExecutiveNotFound() {
        User user = new User();
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setName(BranchName.ANNA_NAGAR);
        Executive manager = new Executive();
        manager.setId(1L);
        manager.setBranch(branch);
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setId(1L);
        accountRequest.setRequestType(RequestType.OPEN);
        accountRequest.setBranchName("ANNA_NAGAR");
        accountRequest.setCustomer(customer);

        when(accountRequestRepository.findById(1L)).thenReturn(Optional.of(accountRequest));
        when(executiveRepository.findByUsername("meena")).thenReturn(manager);
        when(executiveRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountRequestService.assignExecutive(1L, 5L, "meena"));

        verify(roleCheckUtility, times(1)).checkManager("meena");
        verify(accountRequestRepository, times(1)).findById(1L);
        verify(executiveRepository, times(1)).findByUsername("meena");
        verify(executiveRepository, times(1)).findById(5L);
        verify(accountRequestRepository, times(0)).save(any(AccountRequest.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRequestRepository);
        verifyNoMoreInteractions(executiveRepository);
    }

    @Test
    public void assignExecutiveTest_InactiveExecutive() {
        // soft deleted executive, so the user is not active
        User user = new User();
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setName(BranchName.ANNA_NAGAR);
        Executive manager = new Executive();
        manager.setId(1L);
        manager.setBranch(branch);
        User executiveUser = new User();
        executiveUser.setActive(false);
        Executive executive = new Executive();
        executive.setId(2L);
        executive.setUser(executiveUser);
        executive.setBranch(branch);
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setId(1L);
        accountRequest.setRequestType(RequestType.OPEN);
        accountRequest.setBranchName("ANNA_NAGAR");
        accountRequest.setCustomer(customer);

        when(accountRequestRepository.findById(1L)).thenReturn(Optional.of(accountRequest));
        when(executiveRepository.findByUsername("meena")).thenReturn(manager);
        when(executiveRepository.findById(2L)).thenReturn(Optional.of(executive));

        assertThrows(InActiveUserException.class, () -> accountRequestService.assignExecutive(1L, 2L, "meena"));

        verify(roleCheckUtility, times(1)).checkManager("meena");
        verify(accountRequestRepository, times(1)).findById(1L);
        verify(executiveRepository, times(1)).findByUsername("meena");
        verify(executiveRepository, times(1)).findById(2L);
        verify(accountRequestRepository, times(0)).save(any(AccountRequest.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRequestRepository);
        verifyNoMoreInteractions(executiveRepository);
    }

    @Test
    public void assignExecutiveTest_BranchMismatch() {
        // executive works in another branch
        User user = new User();
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUser(user);
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setName(BranchName.ANNA_NAGAR);
        Branch otherBranch = new Branch();
        otherBranch.setId(2L);
        otherBranch.setName(BranchName.T_NAGAR);
        Executive manager = new Executive();
        manager.setId(1L);
        manager.setBranch(branch);
        User executiveUser = new User();
        Executive executive = new Executive();
        executive.setId(2L);
        executive.setUser(executiveUser);
        executive.setBranch(otherBranch);
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setId(1L);
        accountRequest.setRequestType(RequestType.OPEN);
        accountRequest.setBranchName("ANNA_NAGAR");
        accountRequest.setCustomer(customer);

        when(accountRequestRepository.findById(1L)).thenReturn(Optional.of(accountRequest));
        when(executiveRepository.findByUsername("meena")).thenReturn(manager);
        when(executiveRepository.findById(2L)).thenReturn(Optional.of(executive));

        assertThrows(IllegalArgumentException.class, () -> accountRequestService.assignExecutive(1L, 2L, "meena"));

        verify(roleCheckUtility, times(1)).checkManager("meena");
        verify(accountRequestRepository, times(1)).findById(1L);
        verify(executiveRepository, times(1)).findByUsername("meena");
        verify(executiveRepository, times(1)).findById(2L);
        verify(accountRequestRepository, times(0)).save(any(AccountRequest.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(accountRequestRepository);
        verifyNoMoreInteractions(executiveRepository);
    }

    @Test
    public void dynamicAccountRequestFilterTest_Executive() {
        // plain executive sees only the requests assigned to them in the own branch
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setName(BranchName.ANNA_NAGAR);
        Executive executive = new Executive();
        executive.setId(1L);
        executive.setJobTitle(JobTitle.PERSONAL_BANKER);
        executive.setBranch(branch);
        AccountRequestInfoDto dto1 = new AccountRequestInfoDto(1L, RequestType.OPEN, AccountType.SAVINGS, RequestStatus.IN_PROCESS, "ANNA_NAGAR", null, new BigDecimal("5000"), LocalDate.now(), null, null, 1L, "Ravi Kumar", "Arjun", "arjun@gmail.com");

        when(executiveRepository.findByUsername("arjun")).thenReturn(executive);
        when(accountRequestRepository.dynamicAccountRequestFilter("ANNA_NAGAR", 1L, null, RequestStatus.IN_PROCESS, null, null, null, false, PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(dto1), PageRequest.of(0, 5), 1));

        assertEquals(1, accountRequestService.dynamicAccountRequestFilter(null, null, RequestStatus.IN_PROCESS, null, false, null, null, 0, 5, "arjun").list().size());

        verify(roleCheckUtility, times(1)).checkAdminOrExecutive("arjun");
        verify(executiveRepository, times(1)).findByUsername("arjun");
        verify(accountRequestRepository, times(1)).dynamicAccountRequestFilter("ANNA_NAGAR", 1L, null, RequestStatus.IN_PROCESS, null, null, null, false, PageRequest.of(0, 5));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(accountRequestRepository);
    }

    @Test
    public void dynamicAccountRequestFilterTest_Manager() {
        // manager sees the whole branch, so executive id is null
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setName(BranchName.ANNA_NAGAR);
        Executive manager = new Executive();
        manager.setId(2L);
        manager.setJobTitle(JobTitle.BANK_MANAGER);
        manager.setBranch(branch);
        AccountRequestInfoDto dto1 = new AccountRequestInfoDto(1L, RequestType.OPEN, AccountType.SAVINGS, RequestStatus.IN_PROCESS, "ANNA_NAGAR", null, new BigDecimal("5000"), LocalDate.now(), null, null, 1L, "Ravi Kumar", null, null);
        AccountRequestInfoDto dto2 = new AccountRequestInfoDto(2L, RequestType.OPEN, AccountType.BUSINESS, RequestStatus.IN_PROCESS, "ANNA_NAGAR", null, null, LocalDate.now(), null, null, 2L, "Priya Sundaram", null, null);

        when(executiveRepository.findByUsername("meena")).thenReturn(manager);
        when(accountRequestRepository.dynamicAccountRequestFilter("ANNA_NAGAR", null, null, null, null, null, null, true, PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(dto1, dto2), PageRequest.of(0, 5), 2));

        assertEquals(2, accountRequestService.dynamicAccountRequestFilter(null, null, null, null, true, null, null, 0, 5, "meena").list().size());

        verify(roleCheckUtility, times(1)).checkAdminOrExecutive("meena");
        verify(executiveRepository, times(1)).findByUsername("meena");
        verify(accountRequestRepository, times(1)).dynamicAccountRequestFilter("ANNA_NAGAR", null, null, null, null, null, null, true, PageRequest.of(0, 5));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(accountRequestRepository);
    }

    @Test
    public void dynamicAccountRequestFilterTest_Admin() {
        // admin is not an executive, so the branch comes from the filter
        AccountRequestInfoDto dto1 = new AccountRequestInfoDto(1L, RequestType.CLOSE, AccountType.SAVINGS, RequestStatus.APPROVED, "T_NAGAR", "Moving to another city", null, LocalDate.now(), LocalDate.now(), null, 3L, "Karthik Raja", "Arjun", "arjun@gmail.com");

        when(executiveRepository.findByUsername("admin")).thenReturn(null);
        when(accountRequestRepository.dynamicAccountRequestFilter("T_NAGAR", null, RequestType.CLOSE, null, null, null, null, false, PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(dto1), PageRequest.of(0, 5), 1));

        assertEquals(1, accountRequestService.dynamicAccountRequestFilter(BranchName.T_NAGAR, RequestType.CLOSE, null, null, false, null, null, 0, 5, "admin").list().size());

        verify(roleCheckUtility, times(1)).checkAdminOrExecutive("admin");
        verify(executiveRepository, times(1)).findByUsername("admin");
        verify(accountRequestRepository, times(1)).dynamicAccountRequestFilter("T_NAGAR", null, RequestType.CLOSE, null, null, null, null, false, PageRequest.of(0, 5));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(accountRequestRepository);
    }

    @Test
    public void getAccountRequestsByStatusTest_Executive() {
        // executive sees only the requests of the own branch
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setName(BranchName.ANNA_NAGAR);
        Executive executive = new Executive();
        executive.setId(1L);
        executive.setBranch(branch);

        when(executiveRepository.findByUsername("arjun")).thenReturn(executive);
        when(accountRequestRepository.getRequestCountByStatus("ANNA_NAGAR", RequestStatus.IN_PROCESS)).thenReturn(3L);
        when(accountRequestRepository.getRequestCountByStatus("ANNA_NAGAR", RequestStatus.REJECTED)).thenReturn(1L);
        when(accountRequestRepository.getRequestCountByStatus("ANNA_NAGAR", RequestStatus.APPROVED)).thenReturn(5L);

        StatDto statDto = accountRequestService.getAccountRequestsByStatus("arjun");
        assertEquals(List.of(3L, 1L, 5L), statDto.values());

        verify(roleCheckUtility, times(1)).checkAdminOrExecutive("arjun");
        verify(executiveRepository, times(1)).findByUsername("arjun");
        verify(accountRequestRepository, times(1)).getRequestCountByStatus("ANNA_NAGAR", RequestStatus.IN_PROCESS);
        verify(accountRequestRepository, times(1)).getRequestCountByStatus("ANNA_NAGAR", RequestStatus.REJECTED);
        verify(accountRequestRepository, times(1)).getRequestCountByStatus("ANNA_NAGAR", RequestStatus.APPROVED);

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(accountRequestRepository);
    }

    @Test
    public void getAccountRequestsByStatusTest_Admin() {
        // admin is not an executive, so branch is null and all requests are counted
        when(executiveRepository.findByUsername("admin")).thenReturn(null);
        when(accountRequestRepository.getRequestCountByStatus(null, RequestStatus.IN_PROCESS)).thenReturn(8L);
        when(accountRequestRepository.getRequestCountByStatus(null, RequestStatus.REJECTED)).thenReturn(2L);
        when(accountRequestRepository.getRequestCountByStatus(null, RequestStatus.APPROVED)).thenReturn(12L);

        StatDto statDto = accountRequestService.getAccountRequestsByStatus("admin");
        assertEquals(List.of(8L, 2L, 12L), statDto.values());

        verify(roleCheckUtility, times(1)).checkAdminOrExecutive("admin");
        verify(executiveRepository, times(1)).findByUsername("admin");
        verify(accountRequestRepository, times(1)).getRequestCountByStatus(null, RequestStatus.IN_PROCESS);
        verify(accountRequestRepository, times(1)).getRequestCountByStatus(null, RequestStatus.REJECTED);
        verify(accountRequestRepository, times(1)).getRequestCountByStatus(null, RequestStatus.APPROVED);

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(accountRequestRepository);
    }
}
