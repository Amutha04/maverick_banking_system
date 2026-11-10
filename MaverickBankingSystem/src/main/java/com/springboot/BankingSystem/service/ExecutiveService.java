package com.springboot.BankingSystem.service;

import com.springboot.BankingSystem.dto.request.ExecutiveDto;
import com.springboot.BankingSystem.dto.response.ExecutiveInfoDto;
import com.springboot.BankingSystem.dto.response.PageResponseDto;
import com.springboot.BankingSystem.dto.response.TeamMemberDto;
import com.springboot.BankingSystem.enums.BranchName;
import com.springboot.BankingSystem.enums.JobTitle;
import com.springboot.BankingSystem.enums.Role;
import com.springboot.BankingSystem.exception.DuplicateRequestException;
import com.springboot.BankingSystem.exception.InvalidAccountStateException;
import com.springboot.BankingSystem.exception.ResourceNotFoundException;
import com.springboot.BankingSystem.mapper.ExecutiveMapper;
import com.springboot.BankingSystem.mapper.PageMapper;
import com.springboot.BankingSystem.model.Branch;
import com.springboot.BankingSystem.model.Executive;
import com.springboot.BankingSystem.model.User;
import com.springboot.BankingSystem.repository.AccountRequestRepository;
import com.springboot.BankingSystem.repository.BranchRepository;
import com.springboot.BankingSystem.repository.LoanRepository;
import com.springboot.BankingSystem.repository.ExecutiveRepository;
import com.springboot.BankingSystem.repository.UserRepository;
import com.springboot.BankingSystem.utility.RoleCheckUtility;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExecutiveService {
    private final ExecutiveRepository executiveRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleCheckUtility roleCheckUtility;
    private final AccountRequestRepository accountRequestRepository;
    private final LoanRepository loanRepository;

    @Transactional
    public void add(@Valid ExecutiveDto executiveDto, String loggedUsername) {
        // 1. check the logged-in user is admin or not
        roleCheckUtility.checkAdmin(loggedUsername);

        // 2. the username must be free, the column is unique and a deactivated user keeps the name
        if(userRepository.existsByUsername(executiveDto.username()))
            throw new DuplicateRequestException("Username already exists");

        // 3. create user, branch obj and save that in  db
        User user = new User();
        user.setUsername(executiveDto.username());
        user.setPassword(passwordEncoder.encode(executiveDto.password()));
        user.setRole(Role.EXECUTIVE);
        userRepository.save(user);
        log.info("User is added");

        Branch branch = branchRepository.findById(executiveDto.branchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

        // 4. create executive obj and map the details from executiveDto
        Executive executive = ExecutiveMapper.convertDtoToExecutive(executiveDto);

        // 5. add user, branch obj into executive obj
        executive.setUser(user);
        executive.setBranch(branch);

        // 6. save executive obj in db
        executiveRepository.save(executive);
        log.info("Executive is added");
    }

    public void delete(Long executiveId, String loggedUsername) {
        // 1. check the logged-in user is admin or not
        roleCheckUtility.checkAdmin(loggedUsername);

        // 2. get executive by id
        Executive executive = executiveRepository.findById(executiveId)
                .orElseThrow(() -> new ResourceNotFoundException("Executive id not found"));

        // 3. the executive must not hold any pending work, nobody else could act on it after the deactivation
        long pendingRequests = accountRequestRepository.getPendingCountByExecutive(executiveId);
        long pendingLoans = loanRepository.getPendingCountByExecutive(executiveId);
        if(pendingRequests > 0 || pendingLoans > 0)
            throw new InvalidAccountStateException("Executive still holds " + pendingRequests + " pending requests and " + pendingLoans + " pending loans");

        // 4. set user active as false to softDelete
        executive.getUser().setActive(false);

        // 5. save in db
        executiveRepository.save(executive);
        log.info("Executive is deleted");
    }

    public List<ExecutiveInfoDto> getAll(String loggedUsername, int page, int size) {
        // 1. check the logged-in user is admin or not
        roleCheckUtility.checkAdmin(loggedUsername);

        // 2. Set page and size
        Pageable pageable = PageRequest.of(page, size);
        // 3. get all customers
        return executiveRepository.getAll(pageable);
    }

    public ExecutiveInfoDto getById(String loggedUsername, Long id) {
        // 1. check the logged-in user is admin or not
        roleCheckUtility.checkAdmin(loggedUsername);

        // 2. get executive by id
        Executive executive = executiveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Executive id not found"));

        // 3. convert executive into dto and return
        return ExecutiveMapper.convertExecutiveToDto(executive);
    }

    public PageResponseDto<ExecutiveInfoDto> dynamicExecutiveFilter(String name, JobTitle jobTitle, String username, BranchName branchName, LocalDate createdFrom, LocalDate createdTo, int page, int size, String loggedUsername) {
        // 1. check the logged-in user is admin or not
        roleCheckUtility.checkAdmin(loggedUsername);

        // 2. Set page and size
        Pageable pageable = PageRequest.of(page, size);

        // 3. get the list with the pagination info
        return PageMapper.convertPageToDto(executiveRepository.dynamicExecutiveFilter(name, jobTitle, username, branchName, createdFrom, createdTo, pageable));
    }

    public ExecutiveInfoDto get(String loggedUsername) {
        // 1. get executive by the logged-in username
        Executive executive = executiveRepository.findByUsername(loggedUsername);

        // 2. convert executive into dto and return
        return ExecutiveMapper.convertExecutiveToDto(executive);
    }

    public void updateJobTitle(long executiveId, JobTitle jobTitle, String loggedUsername) {
        // 1. check the logged-in user is admin or not
        roleCheckUtility.checkAdmin(loggedUsername);

        // 2. get executive by id
        Executive executive = executiveRepository.findById(executiveId)
                .orElseThrow(() -> new ResourceNotFoundException("Executive id not found"));

        // 3. set the job title
        executive.setJobTitle(jobTitle);

        // 4. save in db
        executiveRepository.save(executive);
        log.info("JobTitle is updated");
    }

    public List<TeamMemberDto> getTeam(String loggedUsername) {
        // 1. check the logged user belongs to manager
        roleCheckUtility.checkManager(loggedUsername);

        // 2. get the executives of the manager's branch with the work they hold
        Executive manager = executiveRepository.findByUsername(loggedUsername);
        List<TeamMemberDto> team = new ArrayList<>();

        for(Executive executive : executiveRepository.getByBranch(manager.getBranch().getId())) {
            team.add(new TeamMemberDto(
                    executive.getId(),
                    executive.getName(),
                    executive.getEmail(),
                    executive.getContact(),
                    executive.getJobTitle(),
                    accountRequestRepository.getPendingCountByExecutive(executive.getId()),
                    loanRepository.getPendingCountByExecutive(executive.getId())
            ));
        }

        return team;
    }
}
