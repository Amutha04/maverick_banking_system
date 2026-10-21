package com.springboot.BankingSystem.repository;

import com.springboot.BankingSystem.dto.response.BeneficiaryInfoDto;
import com.springboot.BankingSystem.model.Beneficiaries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiaries, Long> {
    @Query("""
select count(b.id) > 0
from Beneficiaries b
join b.customer c
where c.id = ?1
AND b.accountNumber = ?2
""")
    boolean isExists(Long customerId, Long accountNumber);

    @Query("""
select b.id, b.name, b.accountNumber, b.addedOn
from Beneficiaries b
join b.customer c
join c.user u
where u.username = ?1
""")
    List<BeneficiaryInfoDto> getByUsername(String username);
}
