package com.springboot.BankingSystem.service;

import com.springboot.BankingSystem.dto.request.CustomerDto;
import com.springboot.BankingSystem.dto.response.CustomerInfoDto;
import com.springboot.BankingSystem.dto.response.CustomerRespDto;
import com.springboot.BankingSystem.enums.Gender;
import com.springboot.BankingSystem.enums.KYCStatus;
import com.springboot.BankingSystem.enums.Role;
import com.springboot.BankingSystem.exception.OwnershipException;
import com.springboot.BankingSystem.exception.ResourceNotFoundException;
import com.springboot.BankingSystem.mapper.CustomerMapper;
import com.springboot.BankingSystem.model.Customer;
import com.springboot.BankingSystem.model.User;
import com.springboot.BankingSystem.repository.CustomerRepository;
import com.springboot.BankingSystem.repository.UserRepository;
import com.springboot.BankingSystem.utility.RoleCheckUtility;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleCheckUtility roleCheckUtility;

    public CustomerRespDto getById(Long id, String loggedUsername) {
        // 1. check the logged user belongs to admin or executives
        User user = roleCheckUtility.checkAdminOrExecutive(loggedUsername);

        // 2. get customer obj by id
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer id not found"));
        if(!customer.getUser().isActive())
            throw new ResourceNotFoundException("User not found");

        // 3. convert customer into dto and return
        return CustomerMapper.convertCustomerToDto(customer);
    }


    public void add(@Valid CustomerDto customerDto) {
        // 1. create user obj and save that in  db
        User user = new User();
        user.setUsername(customerDto.username());
        user.setPassword(passwordEncoder.encode(customerDto.password()));
        user.setRole(Role.CUSTOMER);
        userRepository.save(user);

        // 2. create customer obj and map the details from customerDto
        Customer customer = CustomerMapper.convertDtoToCustomer(customerDto);

        // 3. add user obj into customer obj
        customer.setUser(user);

        // 4. save customer obj in db
        customerRepository.save(customer);
    }

    public void delete(Long customerId, String loggedUsername) {
        // 1. check the logged-in user is admin or not
        User user = roleCheckUtility.checkAdmin(loggedUsername);

        // 2. get customer by id
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer id not found"));

        // 3. set user active as false to softDelete
        customer.getUser().setActive(false);

        // 4. save in db
        customerRepository.save(customer);
    }

    public List<CustomerInfoDto> getAll(int page, int size, String loggedUsername) {
        // 1. check the logged user belongs to admin or executives
        User user = roleCheckUtility.checkAdminOrExecutive(loggedUsername);

        // 2. Set page and size
        Pageable pageable = PageRequest.of(page, size);

        //3. get all customers
        return customerRepository.getAll(pageable);
    }

    public List<CustomerInfoDto> dynamicCustomerFilter(String name, Gender gender, Long aadhaarNumber, String username, LocalDate createdFrom, LocalDate createdTo, int page, int size) {
        // 1. set page and size
        Pageable pageable = PageRequest.of(page, size);

        // 2. get the list from repo and return
        return customerRepository.dynamicCustomerFilter(name, gender, aadhaarNumber, username, createdFrom, createdTo, pageable);
    }

    public CustomerRespDto get(String loggedUsername) {
        // 1. get customer obj by logged-in username
        Customer customer = customerRepository.findByUsername(loggedUsername);

        // 2. convert into dto and return
        return CustomerMapper.convertCustomerToDto(customer);
    }

    public void updateKYC(Long customerId, String loggedUsername) {
        // 1. check the logged user belongs to executive
        User user = roleCheckUtility.checkExecutive(loggedUsername);

        // 2. get the customer obj and update the kyc status
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        customer.setKycStatus(KYCStatus.DONE);

        // 3. save it
        customerRepository.save(customer);
    }
}
