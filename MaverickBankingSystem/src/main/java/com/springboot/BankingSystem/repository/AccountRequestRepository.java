package com.springboot.BankingSystem.repository;

import com.springboot.BankingSystem.dto.response.AccountRequestInfoDto;
import com.springboot.BankingSystem.enums.AccountType;
import com.springboot.BankingSystem.enums.RequestStatus;
import com.springboot.BankingSystem.enums.RequestType;
import com.springboot.BankingSystem.model.AccountRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AccountRequestRepository extends JpaRepository<AccountRequest, Long> {

    @Query("""
select count(a.id) > 0
from AccountRequest a
left join a.customer c
left join a.account ac
where c.id = ?1
AND a.accountType = ?2
AND (a.requestStatus = RequestStatus.IN_PROCESS
OR (a.requestStatus = RequestStatus.APPROVED AND ac.status != AccountStatus.CLOSED))
""")
    boolean isExistsForOpen(Long id, AccountType accountType);

    @Query(value = """
select a.id, a.requestType, a.accountType, a.requestStatus, a.branchName, a.reason, a.initialDeposit, a.requestedOn, a.processedOn, a.remark, c.id, c.name, e.name, e.email
from AccountRequest a
join a.customer c
left join a.executive e
join c.user u
where u.username = ?1
AND (?2 is null OR a.requestType = ?2)
AND (?3 is null OR a.requestStatus = ?3)
AND (?4 is null OR a.requestedOn >= ?4)
AND (?5 is null OR a.requestedOn <= ?5)
""")
    Page<AccountRequestInfoDto> getByUsername(String loggedUsername, RequestType requestType, RequestStatus requestStatus, LocalDate requestedFrom, LocalDate requestedTo, Pageable pageable);

    @Query(value = """
select a.id, a.requestType, a.accountType, a.requestStatus, a.branchName, a.reason, a.initialDeposit, a.requestedOn, a.processedOn, a.remark, c.id, c.name, e.name, e.email
from AccountRequest a
join a.customer c
left join a.executive e
where (?1 is null OR a.branchName = ?1)
AND (?2 is null OR e.id = ?2)
AND (?3 is null OR a.requestType = ?3)
AND (?4 is null OR a.requestStatus = ?4)
AND (?5 is null OR c.name = ?5)
AND (?6 is null OR a.requestedOn >= ?6)
AND (?7 is null OR a.requestedOn <= ?7)
AND (?8 = false OR e.id is null)
""")
    Page<AccountRequestInfoDto> dynamicAccountRequestFilter(String branchName, Long executiveId, RequestType requestType, RequestStatus requestStatus, String customerName, LocalDate requestedFrom, LocalDate requestedTo, boolean unassigned, Pageable pageable);

    @Query("""
select count(a.id)
from AccountRequest a
where (?1 is null OR a.branchName = ?1)
AND a.requestStatus = ?2
""")
    long getRequestCountByStatus(String branchName, RequestStatus requestStatus);

    @Query("""
select count(a.id)
from AccountRequest a
join a.executive e
where e.id = ?1
AND a.requestStatus = RequestStatus.IN_PROCESS
""")
    long getPendingCountByExecutive(Long executiveId);
}
