package com.springboot.BankingSystem.mapper;

import com.springboot.BankingSystem.dto.request.CustomerDto;
import com.springboot.BankingSystem.dto.response.CustomerRespDto;
import com.springboot.BankingSystem.model.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public static Customer convertDtoToCustomer(CustomerDto customerDto) {
        Customer customer = new Customer();
        customer.setName(customerDto.name());
        customer.setGender(customerDto.gender());
        customer.setDob(customerDto.dob());
        customer.setAddress(customerDto.address());
        customer.setEmail(customerDto.email());
        customer.setContact(customerDto.contact());
        customer.setAadhaarNumber(customerDto.aadhaarNumber());
        customer.setPanNumber(customerDto.panNumber());
        return customer;
    }

    public static CustomerRespDto convertCustomerToDto(Customer customer) {
        return new CustomerRespDto(
                customer.getName(),
                customer.getGender(),
                customer.getDob(),
                customer.getAddress(),
                customer.getEmail(),
                customer.getContact(),
                customer.getAadhaarNumber(),
                customer.getPanNumber(),
                customer.getUpdatedAt(),
                customer.getUser().getUsername()
        );
    }
}
