package com.springboot.BankingSystem.service;

import com.springboot.BankingSystem.dto.request.CustomerDto;
import com.springboot.BankingSystem.dto.response.CustomerInfoDto;
import com.springboot.BankingSystem.dto.response.CustomerRespDto;
import com.springboot.BankingSystem.enums.Gender;
import com.springboot.BankingSystem.enums.Role;
import com.springboot.BankingSystem.exception.ResourceNotFoundException;
import com.springboot.BankingSystem.mapper.CustomerMapper;
import com.springboot.BankingSystem.model.Customer;
import com.springboot.BankingSystem.model.User;
import com.springboot.BankingSystem.repository.CustomerRepository;
import com.springboot.BankingSystem.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    public CustomerRespDto getById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer id not found"));
        return CustomerMapper.convertCustomerToDto(customer);
    }


    public void add(@Valid CustomerDto customerDto) {
        // 1. create user obj and save that in  db
        User user = new User();
        user.setUsername(customerDto.username());
        user.setPassword(customerDto.password());
        user.setRole(Role.CUSTOMER);
        userRepository.save(user);

        // 2. create customer obj and map the details from customerDto
        Customer customer = CustomerMapper.convertDtoToCustomer(customerDto);

        // 3. add user obj into customer obj
        customer.setUser(user);

        // 4. save customer obj in db
        customerRepository.save(customer);
    }

    public void delete(Long customerId) {
        // 1. get customer by id
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer id not found"));
        // 2. set user active as false to softDelete
        customer.getUser().setActive(false);
        // 3. save in db
        customerRepository.save(customer);
    }

    public List<CustomerInfoDto> getAll(int page, int size) {
        // 1. Set page and size
        Pageable pageable = PageRequest.of(page, size);
        //2. get all customers
        return customerRepository.getAll(pageable);
    }

    public List<CustomerInfoDto> dynamicCustomerFilter(String name, Gender gender, Long aadhaarNumber, String username, LocalDate createdFrom, LocalDate createdTo, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return customerRepository.dynamicCustomerFilter(name, gender, aadhaarNumber, username, createdFrom, createdTo, pageable);
    }
}
