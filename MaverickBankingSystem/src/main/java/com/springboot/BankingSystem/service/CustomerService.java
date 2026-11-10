package com.springboot.BankingSystem.service;

import com.springboot.BankingSystem.dto.request.CustomerDto;
import com.springboot.BankingSystem.dto.response.CustomerInfoDto;
import com.springboot.BankingSystem.dto.response.CustomerRespDto;
import com.springboot.BankingSystem.dto.response.PageResponseDto;
import com.springboot.BankingSystem.enums.Gender;
import com.springboot.BankingSystem.enums.KYCStatus;
import com.springboot.BankingSystem.enums.Role;
import com.springboot.BankingSystem.exception.DuplicateRequestException;
import com.springboot.BankingSystem.exception.InvalidAccountStateException;
import com.springboot.BankingSystem.exception.ResourceNotFoundException;
import com.springboot.BankingSystem.mapper.CustomerMapper;
import com.springboot.BankingSystem.mapper.PageMapper;
import com.springboot.BankingSystem.model.Customer;
import com.springboot.BankingSystem.model.User;
import com.springboot.BankingSystem.repository.AccountRepository;
import com.springboot.BankingSystem.repository.CustomerRepository;
import com.springboot.BankingSystem.repository.LoanRepository;
import com.springboot.BankingSystem.repository.UserRepository;
import com.springboot.BankingSystem.utility.RoleCheckUtility;
import com.springboot.BankingSystem.dto.response.StatDto;
import com.springboot.BankingSystem.repository.ExecutiveRepository;
import com.springboot.BankingSystem.model.Executive;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleCheckUtility roleCheckUtility;
    private final AccountRepository accountRepository;
    private final LoanRepository loanRepository;
    private final ExecutiveRepository executiveRepository;

    @Value("${app.upload-dir}")
    private String uploadDir;

    public CustomerRespDto getById(Long id, String loggedUsername) {
        // 1. check the logged user belongs to admin or executives
        roleCheckUtility.checkAdminOrExecutive(loggedUsername);

        // 2. get customer obj by id
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer id not found"));
        if(!customer.getUser().isActive())
            throw new ResourceNotFoundException("User not found");

        // 3. convert customer into dto and return
        return CustomerMapper.convertCustomerToDto(customer);
    }

    @Transactional
    public void add(@Valid CustomerDto customerDto) {
        // 1. the username must be free, the column is unique and a deactivated user keeps the name
        if(userRepository.existsByUsername(customerDto.username()))
            throw new DuplicateRequestException("Username already exists");

        // 2. create user obj and save that in  db
        User user = new User();
        user.setUsername(customerDto.username());
        user.setPassword(passwordEncoder.encode(customerDto.password()));
        user.setRole(Role.CUSTOMER);
        userRepository.save(user);
        log.info("User is added");

        // 3. create customer obj and map the details from customerDto
        Customer customer = CustomerMapper.convertDtoToCustomer(customerDto);

        // 4. add user obj into customer obj
        customer.setUser(user);

        // 5. save customer obj in db
        customerRepository.save(customer);
        log.info("Customer is added");
    }

    public void delete(Long customerId, String loggedUsername) {
        // 1. check the logged-in user is admin or not
        roleCheckUtility.checkAdmin(loggedUsername);

        // 2. get customer by id
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer id not found"));

        // 3. check the open accounts
        if(accountRepository.getAccountByCustomerId(customer.getId()))
            throw new InvalidAccountStateException("Accounts should be closed");

        // 4. check the loan status
        if(loanRepository.getLoanByCustomerId(customer.getId()))
            throw new InvalidAccountStateException("Loans should be closed");

        // 5. set user active as false to softDelete
        customer.getUser().setActive(false);

        // 6. save in db
        customerRepository.save(customer);
        log.info("Customer is deleted");
    }

    public List<CustomerInfoDto> getAll(int page, int size, String loggedUsername) {
        // 1. check the logged user belongs to admin or executives
        roleCheckUtility.checkAdminOrExecutive(loggedUsername);

        // 2. Set page and size
        Pageable pageable = PageRequest.of(page, size);

        //3. get all customers
        return customerRepository.getAll(pageable);
    }

    public PageResponseDto<CustomerInfoDto> dynamicCustomerFilter(String name, Gender gender, Long aadhaarNumber, String username, KYCStatus kycStatus, LocalDate createdFrom, LocalDate createdTo, int page, int size, String loggedUsername) {
        // 1. check the logged user belongs to admin or executives
        roleCheckUtility.checkAdminOrExecutive(loggedUsername);

        // 2. set page and size
        Pageable pageable = PageRequest.of(page, size);

        // 3. get the list with the pagination info
        return PageMapper.convertPageToDto(customerRepository.dynamicCustomerFilter(name, gender, aadhaarNumber, username, kycStatus, createdFrom, createdTo, pageable));
    }

    public CustomerRespDto get(String loggedUsername) {
        // 1. get customer obj by logged-in username
        Customer customer = customerRepository.findByUsername(loggedUsername);

        // 2. convert into dto and return
        return CustomerMapper.convertCustomerToDto(customer);
    }

    public void updateKYC(Long customerId, String loggedUsername) {
        // 1. check the logged user belongs to executive
        roleCheckUtility.checkExecutive(loggedUsername);

        // 2. get the customer obj and update the kyc status
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        customer.setKycStatus(KYCStatus.DONE);

        // 3. save it
        customerRepository.save(customer);
        log.info("KYC is updated");
    }

    public void uploadKyc(MultipartFile aadhaar, MultipartFile pan, MultipartFile photo, MultipartFile signature, String loggedUsername) throws IOException {
        // 1. check the logged user belongs to customer
        roleCheckUtility.checkCustomer(loggedUsername);

        // 2. check the file is empty or not, the 2 MB size limit is set in application.properties
        if(aadhaar == null || aadhaar.isEmpty() || pan == null || pan.isEmpty() || photo == null || photo.isEmpty() || signature == null || signature.isEmpty())
            throw new IllegalArgumentException("Please upload valid file");

        // 3. save the files in local system
        String aadhaarFileName = System.currentTimeMillis() + "_" + aadhaar.getOriginalFilename();
        String panFileName = System.currentTimeMillis() + "_" + pan.getOriginalFilename();
        String photoFileName = System.currentTimeMillis() + "_" + photo.getOriginalFilename();
        String signatureFileName = System.currentTimeMillis() + "_" + signature.getOriginalFilename();

        Files.copy(aadhaar.getInputStream(), Paths.get(uploadDir + aadhaarFileName), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(pan.getInputStream(), Paths.get(uploadDir + panFileName), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(photo.getInputStream(), Paths.get(uploadDir + photoFileName), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(signature.getInputStream(), Paths.get(uploadDir + signatureFileName), StandardCopyOption.REPLACE_EXISTING);

        // save filename in db
        Customer customer = customerRepository.findByUsername(loggedUsername);

        customer.setAadhaarUrl(aadhaarFileName);
        customer.setPanUrl(panFileName);
        customer.setPhotoUrl(photoFileName);
        customer.setSignatureUrl(signatureFileName);

        // 4. make kyc status as pending, because when the executive approve, they will verify the kyc again
        customer.setKycStatus(KYCStatus.PENDING);
        customerRepository.save(customer);

        log.info("File is uploaded");

    }

    public StatDto getCustomersByKyc(String loggedUsername) {
        // 1. check the logged user belongs to admin or executives
        roleCheckUtility.checkAdminOrExecutive(loggedUsername);

        // 2. executive sees only the customers who requested in the own branch, admin sees all
        Executive executive = executiveRepository.findByUsername(loggedUsername);
        String branchName = executive == null ? null : executive.getBranch().getName().toString();

        // 3. count the customers for each kyc status, the dashboard chart shows them
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        for(KYCStatus status : KYCStatus.values()) {
            labels.add(status.toString());
            values.add(customerRepository.getCustomerCountByKyc(branchName, status));
        }

        return new StatDto(labels, values);
    }
}
