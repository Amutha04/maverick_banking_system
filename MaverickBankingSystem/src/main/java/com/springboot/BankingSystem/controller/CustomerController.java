package com.springboot.BankingSystem.controller;

import com.springboot.BankingSystem.model.Customer;
import com.springboot.BankingSystem.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/api/customer/add")
    public Customer insertCustomer(@RequestBody Customer customer) {
        return customerService.insertCustomer(customer);
    }

    @GetMapping("/api/customer/{id}")
    public Customer getById(@PathVariable long id) {
        return customerService.getById(id);
    }

    @GetMapping("/api/customer/all")
    public List<Customer> getAll() {
        return customerService.getAll();
    }

    @DeleteMapping("/api/customer/{id}")
    public void deleteById(@PathVariable long id) {
        customerService.deleteById(id);
    }
}
