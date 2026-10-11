package com.springboot.BankingSystem.repository;

import com.springboot.BankingSystem.dto.response.CustomerInfoDto;
import com.springboot.BankingSystem.enums.Gender;
import com.springboot.BankingSystem.model.Customer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("""
select c.name, c.dob, c.email, c.contact, c.aadhaarNumber, c.panNumber, c.user.username
from Customer c
""")
    List<CustomerInfoDto> getAll(Pageable pageable);

    @Query("""
select c.name, c.dob, c.email, c.contact, c.aadhaarNumber, c.panNumber, c.user.username
from Customer c
where (?1 is null OR c.name = ?1)
AND (?2 is null OR c.gender = ?2)
AND (?3 is null OR c.aadhaarNumber = ?3)
AND (?4 is null OR c.user.username = ?4)
AND (?5 is null OR c.user.createdAt >= ?5)
AND (?6 is null OR c.user.createdAt <= ?6)
""")
    List<CustomerInfoDto> dynamicCustomerFilter(String name, Gender gender, Long aadhaarNumber, String username, LocalDate createdFrom, LocalDate createdTo, Pageable pageable);
}
