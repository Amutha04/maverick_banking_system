package com.springboot.BankingSystem.repository;

import com.springboot.BankingSystem.dto.response.ExecutiveInfoDto;
import com.springboot.BankingSystem.enums.JobTitle;
import com.springboot.BankingSystem.model.Executive;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExecutiveRepository extends JpaRepository<Executive, Long> {

    @Query("""
select e.name, e.email, e.contact, e.jobTitle, e.updatedAt, e.user.username
from Executive e
""")
    List<ExecutiveInfoDto> getAll(Pageable pageable);


    @Query("""
select e.name, e.email, e.contact, e.jobTitle, e.updatedAt, e.user.username
from Executive e
where (?1 is null OR e.name = ?1)
AND (?2 is null OR e.jobTitle = ?2)
AND (?3 is null OR e.user.username = ?3)
AND (?4 is null OR e.user.createdAt >= ?4)
AND (?5 is null OR e.user.createdAt <= ?5)
""")
    List<ExecutiveInfoDto> dynamicExecutiveFilter(String name, JobTitle jobTitle, String username, LocalDate createdFrom, LocalDate createdTo, Pageable pageable);
}
