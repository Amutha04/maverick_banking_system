package com.springboot.BankingSystem.repository;

import com.springboot.BankingSystem.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

}
