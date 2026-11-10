package com.springboot.BankingSystem.repository;

import com.springboot.BankingSystem.dto.response.ExecutiveInfoDto;
import com.springboot.BankingSystem.enums.BranchName;
import com.springboot.BankingSystem.enums.JobTitle;
import com.springboot.BankingSystem.model.Executive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExecutiveRepository extends JpaRepository<Executive, Long> {

    @Query("""
select e.id, e.name, e.email, e.contact, e.jobTitle, e.updatedAt, e.user.username, e.branch.name
from Executive e
join e.user u
where u.isActive = true
""")
    List<ExecutiveInfoDto> getAll(Pageable pageable);


    @Query(value = """
select e.id, e.name, e.email, e.contact, e.jobTitle, e.updatedAt, u.username, b.name
from Executive e
join e.user u
join e.branch b
where u.isActive = true
AND (?1 is null OR e.name = ?1)
AND (?2 is null OR e.jobTitle = ?2)
AND (?3 is null OR u.username = ?3)
AND (?4 is null OR b.name = ?4)
AND (?5 is null OR u.createdAt >= ?5)
AND (?6 is null OR u.createdAt <= ?6)
""")
    Page<ExecutiveInfoDto> dynamicExecutiveFilter(String name, JobTitle jobTitle, String username, BranchName branchName, LocalDate createdFrom, LocalDate createdTo, Pageable pageable);

    @Query("""
select e
from Executive e
join e.user u
where u.username = ?1 AND u.isActive = true
""")
    Executive findByUsername(String loggedUsername);

    @Query("""
select e.jobTitle
from Executive e
join e.user u
where u.username = ?1 
""")
    JobTitle getExecutiveJobTitle(String loggedUsername);

    @Query("""
select e
from Executive e
join e.user u
where u.isActive = true
AND e.branch.id = ?1
AND (e.jobTitle is null OR e.jobTitle != JobTitle.BANK_MANAGER)
""")
    List<Executive> getByBranch(Long branchId);
}
