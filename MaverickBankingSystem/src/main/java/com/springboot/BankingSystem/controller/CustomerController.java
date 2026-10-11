package com.springboot.BankingSystem.controller;

import com.springboot.BankingSystem.dto.request.CustomerDto;
import com.springboot.BankingSystem.dto.response.CustomerInfoDto;
import com.springboot.BankingSystem.dto.response.CustomerRespDto;
import com.springboot.BankingSystem.enums.Gender;
import com.springboot.BankingSystem.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/api/customer/add")
    public void add(@Valid @RequestBody CustomerDto customerDto) {
        customerService.add(customerDto);
    }

    @PutMapping("/api/customer/delete/{customerId}")
    public void delete(@PathVariable("customerId") Long customerId) {
        customerService.delete(customerId);
    }

    @GetMapping("/api/customer/all")
    public List<CustomerInfoDto> getAll(@RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                        @RequestParam(name = "size", required = false, defaultValue = "20") int size) {
        return customerService.getAll(page, size);
    }

    @GetMapping("/api/customer/{customerId}")
    public CustomerRespDto getByID(@PathVariable("customerId") Long id) {
        return customerService.getById(id);
    }

    @GetMapping("/api/customer/filter")
    public List<CustomerInfoDto> dynamicCustomerFilter(@RequestParam(name = "name", required = false) String name,
                                      @RequestParam(name = "gender", required = false) Gender gender,
                                      @RequestParam(name = "aadhaarNumber", required = false) Long aadhaarNumber,
                                      @RequestParam(name = "username", required = false) String username,
                                      @RequestParam(name = "createdFrom", required = false) LocalDate createdFrom,
                                      @RequestParam(name = "createdTo", required = false) LocalDate createdTo,
                                      @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                      @RequestParam(name = "size", required = false, defaultValue = "20") int size) {
        return customerService.dynamicCustomerFilter(name, gender, aadhaarNumber, username, createdFrom, createdTo, page, size);

    }

}
