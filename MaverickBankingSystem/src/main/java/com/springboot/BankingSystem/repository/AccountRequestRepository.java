package com.springboot.BankingSystem.repository;

import com.springboot.BankingSystem.enums.RequestType;
import com.springboot.BankingSystem.model.AccountRequest;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRequestRepository extends JpaRepository<AccountRequest, Long> {

    @Query("""
select count(a.id) > 0
from AccountRequest a
join a.customer c
where c.id = ?1
AND a.requestType = ?2
""")
    boolean isExists(Long id, RequestType accountType);
}
