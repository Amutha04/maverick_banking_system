package com.springboot.BankingSystem.service;

import com.springboot.BankingSystem.dto.request.LoanDto;
import com.springboot.BankingSystem.dto.request.RejectRequestDto;
import com.springboot.BankingSystem.dto.request.RepaymentDto;
import com.springboot.BankingSystem.dto.response.LoanInfoDto;
import com.springboot.BankingSystem.enums.*;
import com.springboot.BankingSystem.exception.*;
import com.springboot.BankingSystem.mapper.LoanMapper;
import com.springboot.BankingSystem.model.*;
import com.springboot.BankingSystem.repository.*;
import com.springboot.BankingSystem.utility.RoleCheckUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {
    private final RoleCheckUtility roleCheckUtility;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final LoanRepository loanRepository;
    private final ExecutiveRepository executiveRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;

    public void request(Long accountId, LoanDto loanDto, String loggedUsername) {
        // 1. check the user is CUSTOMER
        roleCheckUtility.checkCustomer(loggedUsername);

        // 2. amount and tenure should be positive
        if(loanDto.amount().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be greater than zero");

        if(loanDto.tenureMonths() <= 0)
            throw new IllegalArgumentException("Tenure must be greater than zero");

        // 3. get the account and check the account holder and logged user are same
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if(!account.getCustomer().getUser().getUsername().equals(loggedUsername))
            throw new OwnershipException("Unauthorized User");

        // 4. check the account status
        if(account.getStatus() != AccountStatus.ACTIVE)
            throw new InvalidAccountStateException("Account is not Active");

        // 5. verify the duplicate request
        if(loanRepository.checkForPendingLoan(account.getId()))
            throw new DuplicateRequestException("Loan request already in process");

        // 6. create loan obj and save in db
        Loan loan = new Loan();
        loan.setLoanType(loanDto.loanType());
        loan.setAmount(loanDto.amount());
        loan.setTenureMonths(loanDto.tenureMonths());
        loan.setInterestRate(BigDecimal.valueOf(getInterestRate(loanDto.loanType())));
        loan.setRequestedOn(LocalDate.now());
        loan.setAccount(account);

        loanRepository.save(loan);
    }

    public void approve(Long loanId, String loggedUsername) {
        // 1. check the user is executive
        roleCheckUtility.checkExecutive(loggedUsername);

        // 2. get the loan and Verify the status
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        Account account = loan.getAccount();
        Customer customer = account.getCustomer();

        if(!customer.getUser().isActive())
            throw new ResourceNotFoundException("Customer not found");

        if(loan.getLoanStatus() != LoanStatus.IN_PROCESS)
            throw new InvalidAccountStateException("Loan is already " + loan.getLoanStatus());

        // 3. check the account status
        if(account.getStatus() != AccountStatus.ACTIVE)
            throw new InvalidAccountStateException("Account is not Active");

        // 4. Verify the KYC status
        if(customer.getKycStatus() == KYCStatus.PENDING)
            throw new IncompleteVerificationException("Incomplete KYC");

        // 5. Verify the age is between 21 and 65
        LocalDate dob = customer.getDob();
        if(dob.isAfter(LocalDate.now().minusYears(21)))
            throw new EligibilityException("Age must be 21 or above");

        if(dob.plusYears(65).isBefore(LocalDate.now().plusMonths(loan.getTenureMonths())))
            throw new EligibilityException("The loan must be fully repaid before you turn 65");

        // 6. Verify the cibil score
        if(customer.getCibilScore() < 650)
            throw new EligibilityException("CIBIL score must be 650 or above");

        // 7. sanction the loan and update in db, amount is disbursed once the customer accepts
        Executive executive = executiveRepository.findByUsername(loggedUsername);

        loan.setLoanStatus(LoanStatus.SANCTIONED);
        loan.setProcessedOn(LocalDate.now());
        loan.setExecutive(executive);

        loanRepository.save(loan);
    }

    public void reject(Long loanId, RejectRequestDto rejectRequestDto, String loggedUsername) {
        // 1. check the user is EXECUTIVE
        roleCheckUtility.checkExecutive(loggedUsername);

        // 2. check the reason if null or not
        if (rejectRequestDto.reason() == null || rejectRequestDto.reason().isBlank())
            throw new IllegalArgumentException("Rejection reason is required");

        // 3. get the loan and Verify the status
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if(!loan.getAccount().getCustomer().getUser().isActive())
            throw new ResourceNotFoundException("Customer not found");

        if(loan.getLoanStatus() != LoanStatus.IN_PROCESS)
            throw new InvalidAccountStateException("Loan is already " + loan.getLoanStatus());

        // 4. reject the loan and update
        Executive executive = executiveRepository.findByUsername(loggedUsername);

        loan.setLoanStatus(LoanStatus.REJECTED);
        loan.setRemark(rejectRequestDto.reason());
        loan.setProcessedOn(LocalDate.now());
        loan.setExecutive(executive);

        loanRepository.save(loan);
    }

    public void accept(Long loanId, String loggedUsername) {
        // 1. check the user is CUSTOMER
        roleCheckUtility.checkCustomer(loggedUsername);

        // 2. get the loan and check the loan holder and logged user are same
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        Account account = loan.getAccount();

        if(!account.getCustomer().getUser().getUsername().equals(loggedUsername))
            throw new OwnershipException("Unauthorized User");

        // 3. Verify the status
        if(loan.getLoanStatus() != LoanStatus.SANCTIONED)
            throw new InvalidAccountStateException("Loan is not Sanctioned");

        // 4. check the account status
        if(account.getStatus() != AccountStatus.ACTIVE)
            throw new InvalidAccountStateException("Account is not Active");

        // 5. credit the loan amount to the account and update
        account.setBalance(account.getBalance().add(loan.getAmount()));
        accountRepository.save(account);

        // 6. save the transaction for the disbursement
        Transaction transaction = new Transaction();
        transaction.setReferenceNumber(transactionService.generateReferenceNumber());
        transaction.setTransactionType(TransactionType.CREDIT);
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);
        transaction.setAmount(loan.getAmount());
        transaction.setDestinationAccount(account.getAccountNumber());
        transaction.setBalance(account.getBalance());
        transaction.setRemarks("Loan disbursed");

        transactionRepository.save(transaction);

        // 7. open the loan with the total amount and update in db
        loan.setLoanStatus(LoanStatus.OPEN);
        loan.setDisbursedDate(LocalDate.now());
        loan.setOutstandingAmount(getTotalAmount(loan));

        loanRepository.save(loan);
    }

    public void decline(Long loanId, RejectRequestDto rejectRequestDto, String loggedUsername) {
        // 1. check the user is CUSTOMER
        roleCheckUtility.checkCustomer(loggedUsername);

        // 2. check the reason if null or not
        if (rejectRequestDto.reason() == null || rejectRequestDto.reason().isBlank())
            throw new IllegalArgumentException("Decline reason is required");

        // 3. get the loan and check the loan holder and logged user are same
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if(!loan.getAccount().getCustomer().getUser().getUsername().equals(loggedUsername))
            throw new OwnershipException("Unauthorized User");

        // 4. Verify the status
        if(loan.getLoanStatus() != LoanStatus.SANCTIONED)
            throw new InvalidAccountStateException("Loan is not Sanctioned");

        // 5. decline the loan and update
        loan.setLoanStatus(LoanStatus.REJECTED);
        loan.setRemark(rejectRequestDto.reason());

        loanRepository.save(loan);
    }

    public void repay(Long loanId, RepaymentDto repaymentDto, String loggedUsername) {
        // 1. check the user is CUSTOMER
        roleCheckUtility.checkCustomer(loggedUsername);

        // 2. amount should be positive
        if(repaymentDto.amount().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be greater than zero");

        // 3. get the loan and check the loan holder and logged user are same
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        Account account = loan.getAccount();

        if(!account.getCustomer().getUser().getUsername().equals(loggedUsername))
            throw new OwnershipException("Unauthorized User");

        // 4. Verify the status
        if(loan.getLoanStatus() != LoanStatus.OPEN)
            throw new InvalidAccountStateException("Loan is not Open");

        // 5. check the account status
        if(account.getStatus() != AccountStatus.ACTIVE)
            throw new InvalidAccountStateException("Account is not Active");

        // 6. amount should not exceed the outstanding amount
        if(repaymentDto.amount().compareTo(loan.getOutstandingAmount()) > 0)
            throw new IllegalArgumentException("Amount exceeds the outstanding amount " + loan.getOutstandingAmount());

        // 7. check the balance is sufficient
        if(account.getBalance().compareTo(repaymentDto.amount()) < 0)
            throw new InsufficientBalanceException("Insufficient balance");

        // 8. debit the amount from the account and update
        account.setBalance(account.getBalance().subtract(repaymentDto.amount()));
        accountRepository.save(account);

        // 9. save the transaction for the repayment
        Transaction transaction = new Transaction();
        transaction.setReferenceNumber(transactionService.generateReferenceNumber());
        transaction.setTransactionType(TransactionType.DEBIT);
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);
        transaction.setAmount(repaymentDto.amount());
        transaction.setBalance(account.getBalance());
        transaction.setRemarks("Loan repayment");
        transaction.setAccount(account);

        transactionRepository.save(transaction);

        // 10. reduce the outstanding amount and close the loan if it is fully paid
        loan.setOutstandingAmount(loan.getOutstandingAmount().subtract(repaymentDto.amount()));

        if(loan.getOutstandingAmount().compareTo(BigDecimal.ZERO) == 0) {
            loan.setLoanStatus(LoanStatus.CLOSED);
            loan.setClosedOn(LocalDate.now());
        }

        loanRepository.save(loan);
    }

    public List<LoanInfoDto> getAll(int page, int size, String loggedUsername) {
        // 1. check the logged user belongs to admin or executives
        User user = roleCheckUtility.checkAdminOrExecutive(loggedUsername);

        // 2. set page and size
        Pageable pageable = PageRequest.of(page, size);

        // 3. get all loans
        return loanRepository.getAll(pageable);
    }

    public List<LoanInfoDto> getByCustomer(int page, int size, Long customerId, String loggedUsername) {
        // 1. check the logged user belongs to admin or executives
        User user = roleCheckUtility.checkAdminOrExecutive(loggedUsername);

        // 2. check the customer exists or not
        customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        // 3. set page and size
        Pageable pageable = PageRequest.of(page, size);

        // 4. get loans by customer
        return loanRepository.getByCustomer(customerId, pageable);
    }

    public List<LoanInfoDto> get(int page, int size, String loggedUsername) {
        // 1. check the user is CUSTOMER
        User user = roleCheckUtility.checkCustomer(loggedUsername);

        // 2. set page and size
        Pageable pageable = PageRequest.of(page, size);

        // 3. get the loans of the logged-in customer
        return loanRepository.getByUsername(loggedUsername, pageable);
    }

    public LoanInfoDto getById(Long loanId, String loggedUsername) {
        // 1. check the user is CUSTOMER
        User user = roleCheckUtility.checkCustomer(loggedUsername);

        // 2. get the loan and check the loan holder and logged user are same
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if(!loan.getAccount().getCustomer().getUser().getUsername().equals(loggedUsername))
            throw new OwnershipException("Unauthorized User");

        // 3. convert the loan into dto and return
        return LoanMapper.convertLoanToDto(loan);
    }

    public List<LoanInfoDto> dynamicLoanFilter(LoanType loanType, LoanStatus loanStatus, Long accountNumber, LocalDate requestedFrom, LocalDate requestedTo, int page, int size, String loggedUsername) {
        // 1. check the logged user belongs to admin or executives
        User user = roleCheckUtility.checkAdminOrExecutive(loggedUsername);

        // 2. set page and size
        Pageable pageable = PageRequest.of(page, size);

        // 3. get the list from repo and return
        return loanRepository.dynamicLoanFilter(loanType, loanStatus, accountNumber, requestedFrom, requestedTo, pageable);
    }

    private int getInterestRate(LoanType loanType) {
        return switch (loanType) {
            case PERSONAL -> 12;
            case HOUSE -> 9;
            case BUSINESS -> 11;
        };
    }

    private BigDecimal getTotalAmount(Loan loan) {
        // simple interest = PRT / 100 -> just for now
        // EMI = P × r × (1+r)^n / ((1+r)^n − 1) -> This is the actual calculation
        BigDecimal interest = loan.getAmount()
                .multiply(loan.getInterestRate())
                .multiply(BigDecimal.valueOf(loan.getTenureMonths()))
                .divide(BigDecimal.valueOf(1200), 2, RoundingMode.HALF_UP);

        return loan.getAmount().add(interest);
    }
}
