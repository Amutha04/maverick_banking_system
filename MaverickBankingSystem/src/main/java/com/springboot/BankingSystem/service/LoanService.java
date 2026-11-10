package com.springboot.BankingSystem.service;

import com.springboot.BankingSystem.dto.request.LoanApproveDto;
import com.springboot.BankingSystem.dto.request.LoanDto;
import com.springboot.BankingSystem.dto.request.RejectRequestDto;
import com.springboot.BankingSystem.dto.request.RepaymentDto;
import com.springboot.BankingSystem.dto.response.LoanInfoDto;
import com.springboot.BankingSystem.dto.response.PageResponseDto;
import com.springboot.BankingSystem.enums.*;
import com.springboot.BankingSystem.exception.*;
import com.springboot.BankingSystem.mapper.LoanMapper;
import com.springboot.BankingSystem.mapper.PageMapper;
import com.springboot.BankingSystem.model.*;
import com.springboot.BankingSystem.repository.*;
import com.springboot.BankingSystem.utility.RoleCheckUtility;
import com.springboot.BankingSystem.dto.response.StatDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanService {
    private final RoleCheckUtility roleCheckUtility;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final LoanRepository loanRepository;
    private final ExecutiveRepository executiveRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;
    private final  LoanProductRepository loanProductRepository;
    private final BranchRepository branchRepository;

    public void request(Long accountId, LoanDto loanDto, String loggedUsername) {
        // 1. check the user is customer
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
        loan.setPurpose(loanDto.purpose());
        loan.setRequestedOn(LocalDate.now());
        loan.setAccount(account);

        loanRepository.save(loan);
        log.info("Loan request is received");
    }

    public void approve(Long loanId, LoanApproveDto loanApproveDto, String loggedUsername) {
        // 1. check the user is executive
        roleCheckUtility.checkExecutive(loggedUsername);

        // 2. the sanctioned amount, tenure and interest rate should be positive
        if(loanApproveDto.amount().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be greater than zero");

        if(loanApproveDto.tenureMonths() <= 0)
            throw new IllegalArgumentException("Tenure must be greater than zero");

        if(loanApproveDto.interestRate().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Interest rate must be greater than zero");

        // 3. get the loan and Verify the status
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        Account account = loan.getAccount();
        Customer customer = account.getCustomer();

        if(!customer.getUser().isActive())
            throw new ResourceNotFoundException("Customer not found");

        if(loan.getLoanStatus() != LoanStatus.IN_PROCESS)
            throw new InvalidAccountStateException("Loan is already " + loan.getLoanStatus());

        // 4. the loan must be assigned to this executive
        Executive executive = executiveRepository.findByUsername(loggedUsername);
        checkAssignment(loan, executive);

        // 5. check the account status
        if(account.getStatus() != AccountStatus.ACTIVE)
            throw new InvalidAccountStateException("Account is not Active");

        // 6. Verify the KYC status
        if(customer.getKycStatus() == KYCStatus.PENDING)
            throw new IncompleteVerificationException("Incomplete KYC");

        // 7. Verify the age is between 21 and 65 with the sanctioned tenure
        LocalDate dob = customer.getDob();
        if(dob.isAfter(LocalDate.now().minusYears(21)))
            throw new EligibilityException("Age must be 21 or above");

        if(dob.plusYears(65).isBefore(LocalDate.now().plusMonths(loanApproveDto.tenureMonths())))
            throw new EligibilityException("The loan must be fully repaid before you turn 65");

        // 8. Verify the cibil score
        if(customer.getCibilScore() < 650)
            throw new EligibilityException("CIBIL score must be 650 or above");

        // 9. sanction the loan with the terms set by the executive, the amount is disbursed after the customer accepts and the manager verifies
        loan.setAmount(loanApproveDto.amount());
        loan.setTenureMonths(loanApproveDto.tenureMonths());
        loan.setInterestRate(loanApproveDto.interestRate());
        loan.setLoanStatus(LoanStatus.SANCTIONED);
        loan.setProcessedOn(LocalDate.now());
        loan.setExecutive(executive);

        loanRepository.save(loan);
        log.info("Loan is sanctioned");
    }

    public void reject(Long loanId, RejectRequestDto rejectRequestDto, String loggedUsername) {
        // 1. check the user is executive
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

        // 4. the loan must be assigned to this executive
        Executive executive = executiveRepository.findByUsername(loggedUsername);
        checkAssignment(loan, executive);

        // 5. reject the loan and update
        loan.setLoanStatus(LoanStatus.REJECTED);
        loan.setRemark(rejectRequestDto.reason());
        loan.setProcessedOn(LocalDate.now());
        loan.setExecutive(executive);

        loanRepository.save(loan);
        log.info("Loan is rejected");
    }

    public void accept(Long loanId, String loggedUsername) {
        // 1. check the user is customer
        roleCheckUtility.checkCustomer(loggedUsername);

        // 2. get the loan and check the loan holder and logged user are same
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        Account account = loan.getAccount();

        if(!account.getCustomer().getUser().getUsername().equals(loggedUsername))
            throw new OwnershipException("Unauthorized User");

        // 3. check the account status
        if(account.getStatus() != AccountStatus.ACTIVE)
            throw new InvalidAccountStateException("Account is not Active");

        // 4. Verify the loan status
        if(loan.getLoanStatus() != LoanStatus.SANCTIONED)
            throw new InvalidAccountStateException("Loan is not Sanctioned");

        // 5. make status as accepted
        loan.setLoanStatus(LoanStatus.ACCEPTED);
        loanRepository.save(loan);
        log.info("Loan is accepted by customer");
    }

    public void decline(Long loanId, RejectRequestDto rejectRequestDto, String loggedUsername) {
        // 1. check the user is customer
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
        log.info("Loan is rejected by customer");
    }

    @Transactional
    public void repay(Long loanId, RepaymentDto repaymentDto, String loggedUsername) {
        // 1. check the user is customer
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

        // 4. verify the status
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
        transaction.setSourceAccount(account.getAccountNumber());
        transaction.setTransactionType(TransactionType.DEBIT);
        transaction.setAmount(repaymentDto.amount());
        transaction.setBalance(account.getBalance());
        transaction.setRemarks("Loan repayment");
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);
        transaction.setAccount(account);

        transactionRepository.save(transaction);
        log.info("Repayment completed successfully");

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
        roleCheckUtility.checkAdminOrExecutive(loggedUsername);

        // 2. set page and size
        Pageable pageable = PageRequest.of(page, size);

        // 3. get all loans
        return loanRepository.getAll(pageable);
    }

    public List<LoanInfoDto> getByCustomer(int page, int size, Long customerId, String loggedUsername) {
        // 1. check the logged user belongs to admin or executives
        roleCheckUtility.checkAdminOrExecutive(loggedUsername);

        // 2. check the customer exists or not
        customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        // 3. set page and size
        Pageable pageable = PageRequest.of(page, size);

        // 4. get loans by customer
        return loanRepository.getByCustomer(customerId, pageable);
    }

    public List<LoanInfoDto> get(int page, int size, String loggedUsername) {
        // 1. check the user is customer
        roleCheckUtility.checkCustomer(loggedUsername);

        // 2. set page and size
        Pageable pageable = PageRequest.of(page, size);

        // 3. get the loans of the logged-in customer
        return loanRepository.getByUsername(loggedUsername, pageable);
    }

    public LoanInfoDto getById(Long loanId, String loggedUsername) {
        // 1. check the user is customer
        roleCheckUtility.checkCustomer(loggedUsername);

        // 2. get the loan and check the loan holder and logged user are same
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if(!loan.getAccount().getCustomer().getUser().getUsername().equals(loggedUsername))
            throw new OwnershipException("Unauthorized User");

        // 3. convert the loan into dto and return
        return LoanMapper.convertLoanToDto(loan);
    }

    public PageResponseDto<LoanInfoDto> dynamicLoanFilter(BranchName branchName, LoanType loanType, LoanStatus loanStatus, Long accountNumber, String customerName, boolean unassigned, LocalDate requestedFrom, LocalDate requestedTo, int page, int size, String loggedUsername) {
        // 1. check the logged user belongs to admin or executives
        roleCheckUtility.checkAdminOrExecutive(loggedUsername);

        // 2. executive sees only the own branch, admin sees the branch chosen in the filter or all
        Executive executive = executiveRepository.findByUsername(loggedUsername);
        Long branchId = null;
        if(executive != null)
            branchId = executive.getBranch().getId();
        else if(branchName != null) {
            Branch branch = branchRepository.findByName(branchName);
            if(branch == null)
                throw new ResourceNotFoundException("Branch not found");
            branchId = branch.getId();
        }

        // 3. a plain executive sees only the loans assigned to them, the manager and the admin see the whole branch
        Long executiveId = (executive != null && executive.getJobTitle() != JobTitle.BANK_MANAGER) ? executive.getId() : null;

        // 4. set page and size
        Pageable pageable = PageRequest.of(page, size);

        // 5. get the list with the pagination info
        return PageMapper.convertPageToDto(loanRepository.dynamicLoanFilter(branchId, executiveId, loanType, loanStatus, accountNumber, customerName, requestedFrom, requestedTo, unassigned, pageable));
    }

    private void checkAssignment(Loan loan, Executive executive) {
        // an assigned loan is handled only by that executive, an unassigned one by any executive of the same branch
        if(loan.getExecutive() != null) {
            if(!loan.getExecutive().getId().equals(executive.getId()))
                throw new OwnershipException("Loan is assigned to another executive");
        }
        else if(!loan.getAccount().getBranch().getId().equals(executive.getBranch().getId()))
            throw new OwnershipException("Loan belongs to another branch");
    }

    private BigDecimal getTotalAmountToPay(Loan loan) {
        // simple interest = PRT / 100 -> just for now
        // EMI = P × r × (1+r)^n / ((1+r)^n − 1) -> This is the actual calculation
        BigDecimal principal = loan.getAmount();
        int n = loan.getTenureMonths();

        // monthly rate = annual % / 12 / 100
        BigDecimal r = loan.getInterestRate()
                .divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);

        // (1 + r)^n
        BigDecimal factor = BigDecimal.ONE.add(r).pow(n, MathContext.DECIMAL128);

        // EMI = P × r × (1+r)^n / ((1+r)^n − 1)
        BigDecimal emi = principal
                .multiply(r)
                .multiply(factor)
                .divide(factor.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);

        return emi.multiply(BigDecimal.valueOf(n));
    }

    public void assignExecutive(Long loanRequestId,Long executiveId, String loggedUsername) {
        // 1. check the logged user belongs to manager
        roleCheckUtility.checkManager(loggedUsername);

        // 2. get the request and Verify the status
        Loan loan = loanRepository.findById(loanRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if(!loan.getAccount().getCustomer().getUser().isActive())
            throw new ResourceNotFoundException("Customer not found");

        if(loan.getLoanStatus() != LoanStatus.IN_PROCESS)
            throw new InvalidAccountStateException("Loan is already " + loan.getLoanStatus());

        if(loan.getExecutive() != null)
            throw new InvalidAccountStateException("Loan is already assigned to " + loan.getExecutive().getName());

        // 3. the loan must belong to the manager's branch
        Executive manager = executiveRepository.findByUsername(loggedUsername);
        if(!loan.getAccount().getBranch().getId().equals(manager.getBranch().getId()))
            throw new OwnershipException("Loan belongs to another branch");

        // 4. check the executive
        Executive executive = executiveRepository.findById(executiveId)
                .orElseThrow(() -> new ResourceNotFoundException("Executive not found"));

        if(!executive.getUser().isActive())
            throw new InActiveUserException("Executive is not active");

        // 5. check the branch
        if(!loan.getAccount().getBranch().getName().equals(executive.getBranch().getName()))
            throw new IllegalArgumentException("Branch mismatched");

        // 6. save the executive
        loan.setExecutive(executive);
        loanRepository.save(loan);
        log.info("Executive is assigned");
    }

    public void verifyLoan(Long loanId, String loggedUsername) {
        // 1. check the logged user belongs to manager
        roleCheckUtility.checkManager(loggedUsername);

        // 2. get the request and Verify the status
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if(!loan.getAccount().getCustomer().getUser().isActive())
            throw new ResourceNotFoundException("Customer not found");

        if(loan.getLoanStatus() != LoanStatus.ACCEPTED)
            throw new InvalidAccountStateException("Get confirmation for the customer");

        // 3. the loan must belong to the manager's branch
        Executive manager = executiveRepository.findByUsername(loggedUsername);
        if(!loan.getAccount().getBranch().getId().equals(manager.getBranch().getId()))
            throw new OwnershipException("Loan belongs to another branch");

        // 4. verify the loan
        loan.setLoanStatus(LoanStatus.VERIFIED);

        // 5. save the changes
        loanRepository.save(loan);
        log.info("Loan is verified by manager");
    }

    @Transactional
    public void disburse(Long loanId, String loggedUsername) {
        // 1. check the logged user belongs to manager
        roleCheckUtility.checkExecutive(loggedUsername);

        // 2. get the request and Verify the status
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if(!loan.getAccount().getCustomer().getUser().isActive())
            throw new ResourceNotFoundException("Customer not found");

        if(loan.getLoanStatus() != LoanStatus.VERIFIED)
            throw new InvalidAccountStateException("Not yet verified by the Manager");

        // 3. the loan must be assigned to this executive
        Executive executive = executiveRepository.findByUsername(loggedUsername);
        checkAssignment(loan, executive);

        // 4. check the account status
        Account account = loan.getAccount();
        if(account.getStatus() != AccountStatus.ACTIVE)
            throw new InvalidAccountStateException("Account is not Active");

        // 5. credit the loan amount to the account and update
        account.setBalance(account.getBalance().add(loan.getAmount()));
        accountRepository.save(account);

        // 6. save the transaction for the disbursement
        Transaction transaction = new Transaction();
        transaction.setReferenceNumber(transactionService.generateReferenceNumber());
        transaction.setTransactionType(TransactionType.CREDIT);
        transaction.setAmount(loan.getAmount());
        transaction.setDestinationAccount(account.getAccountNumber());
        transaction.setBalance(account.getBalance());
        transaction.setRemarks("Loan disbursed");
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);
        transaction.setAccount(account);

        transactionRepository.save(transaction);
        log.info("Transaction completed successfully");

        // 7. open the loan with the total amount and update in db
        loan.setLoanStatus(LoanStatus.OPEN);
        loan.setDisbursedDate(LocalDate.now());
        loan.setOutstandingAmount(getTotalAmountToPay(loan));

        loanRepository.save(loan);
        log.info("Loan is disbursed");
    }

    public void rejectVerification(Long loanId, RejectRequestDto rejectRequestDto, String loggedUsername) {
        // 1. check the logged user belongs to manager
        roleCheckUtility.checkManager(loggedUsername);

        // 2. check the reason if null or not
        if (rejectRequestDto.reason() == null || rejectRequestDto.reason().isBlank())
            throw new IllegalArgumentException("Rejection reason is required");

        // 3. get the loan, only a loan waiting for verification can be rejected here
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if(loan.getLoanStatus() != LoanStatus.ACCEPTED)
            throw new InvalidAccountStateException("Loan is " + loan.getLoanStatus());

        // 4. the loan must belong to the manager's branch
        Executive manager = executiveRepository.findByUsername(loggedUsername);
        if(!loan.getAccount().getBranch().getId().equals(manager.getBranch().getId()))
            throw new OwnershipException("Loan belongs to another branch");

        // 5. reject the loan and update
        loan.setLoanStatus(LoanStatus.REJECTED);
        loan.setRemark(rejectRequestDto.reason());
        loan.setProcessedOn(LocalDate.now());

        loanRepository.save(loan);
        log.info("Verification is failed");
    }

    public StatDto getLoansByStatus(String loggedUsername) {
        // 1. check the logged user belongs to admin or executives
        roleCheckUtility.checkAdminOrExecutive(loggedUsername);

        // 2. executive sees only the own branch, admin sees all
        Executive executive = executiveRepository.findByUsername(loggedUsername);
        Long branchId = executive == null ? null : executive.getBranch().getId();

        // 3. count the loans for each status, the dashboard chart shows them
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        for(LoanStatus status : LoanStatus.values()) {
            labels.add(status.toString());
            values.add(loanRepository.getLoanCountByStatus(branchId, status));
        }

        return new StatDto(labels, values);
    }
}
