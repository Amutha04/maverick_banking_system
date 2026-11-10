package com.springboot.BankingSystem.repository;

import com.springboot.BankingSystem.dto.response.BranchInfoDto;
import com.springboot.BankingSystem.enums.BranchName;
import com.springboot.BankingSystem.model.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    @Query("""
select b.id, b.name, b.state, b.district, b.ifscCode
from Branch b
""")
    List<BranchInfoDto> getAll();

    @Query("""
select b
from Branch b
where b.name = ?1
""")
    Branch findByName(BranchName name);
}
