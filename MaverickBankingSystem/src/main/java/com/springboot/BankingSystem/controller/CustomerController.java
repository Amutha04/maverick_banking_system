package com.springboot.BankingSystem.controller;

import com.springboot.BankingSystem.dto.request.CustomerDto;
import com.springboot.BankingSystem.dto.response.CustomerInfoDto;
import com.springboot.BankingSystem.dto.response.CustomerRespDto;
import com.springboot.BankingSystem.dto.response.PageResponseDto;
import com.springboot.BankingSystem.enums.Gender;
import com.springboot.BankingSystem.enums.KYCStatus;
import com.springboot.BankingSystem.service.CustomerService;
import com.springboot.BankingSystem.dto.response.StatDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/api/customer/add")
    public void add(@Valid @RequestBody CustomerDto customerDto) {
        customerService.add(customerDto);
    }

    @PutMapping("/api/customer/kyc-upload")
    public void uploadKyc(@RequestParam(name = "aadhaar") MultipartFile aadhaar,
                          @RequestParam(name = "pan") MultipartFile pan,
                          @RequestParam(name = "photo") MultipartFile photo,
                          @RequestParam(name = "signature") MultipartFile signature,
                          Principal principal) throws IOException {
        customerService.uploadKyc(aadhaar, pan, photo, signature, principal.getName());
    }

    @PutMapping("/api/customer/delete/{customerId}")
    public void delete(@PathVariable("customerId") Long customerId, Principal principal) {
        customerService.delete(customerId, principal.getName());
    }

    @GetMapping("/api/customer/all")
    public List<CustomerInfoDto> getAll(@RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                        @RequestParam(name = "size", required = false, defaultValue = "20") int size,
                                        Principal principal) {
        return customerService.getAll(page, size, principal.getName());
    }

    @GetMapping("/api/customer/{customerId}")
    public CustomerRespDto getByID(@PathVariable("customerId") Long id,
                                   Principal principal) {
        return customerService.getById(id, principal.getName());
    }

    @GetMapping("/api/customer")
    public CustomerRespDto get(Principal principal) {
        return customerService.get(principal.getName());
    }

    @PutMapping("/api/customer/kyc-update/{customerId}")
    public void updateKYC(@PathVariable Long customerId,
                          Principal principal) {
        customerService.updateKYC(customerId, principal.getName());
    }

    @GetMapping("/api/customer/filter")
    public PageResponseDto<CustomerInfoDto> dynamicCustomerFilter(@RequestParam(name = "name", required = false) String name,
                                      @RequestParam(name = "gender", required = false) Gender gender,
                                      @RequestParam(name = "aadhaarNumber", required = false) Long aadhaarNumber,
                                      @RequestParam(name = "username", required = false) String username,
                                      @RequestParam(name = "kycStatus", required = false) KYCStatus kycStatus,
                                      @RequestParam(name = "createdFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
                                      @RequestParam(name = "createdTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
                                      @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                      @RequestParam(name = "size", required = false, defaultValue = "20") int size,
                                      Principal principal) {
        return customerService.dynamicCustomerFilter(name, gender, aadhaarNumber, username, kycStatus, createdFrom, createdTo, page, size, principal.getName());
    }

    @GetMapping("/api/customer/stat/kyc")
    public StatDto getCustomersByKyc(Principal principal) {
        return customerService.getCustomersByKyc(principal.getName());
    }
}
