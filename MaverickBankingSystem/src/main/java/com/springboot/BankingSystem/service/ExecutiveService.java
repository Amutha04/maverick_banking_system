package com.springboot.BankingSystem.service;

import com.springboot.BankingSystem.dto.request.ExecutiveDto;
import com.springboot.BankingSystem.dto.response.ExecutiveInfoDto;
import com.springboot.BankingSystem.enums.JobTitle;
import com.springboot.BankingSystem.enums.Role;
import com.springboot.BankingSystem.exception.ResourceNotFoundException;
import com.springboot.BankingSystem.mapper.ExecutiveMapper;
import com.springboot.BankingSystem.model.Branch;
import com.springboot.BankingSystem.model.Executive;
import com.springboot.BankingSystem.model.User;
import com.springboot.BankingSystem.repository.BranchRepository;
import com.springboot.BankingSystem.repository.ExecutiveRepository;
import com.springboot.BankingSystem.repository.UserRepository;
import com.springboot.BankingSystem.utility.RoleCheckUtility;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExecutiveService {
    private final ExecutiveRepository executiveRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleCheckUtility roleCheckUtility;

    public void add(@Valid ExecutiveDto executiveDto, String loggedUsername) {
        // 1. check the logged-in user is admin or not
        roleCheckUtility.checkAdmin(loggedUsername);

        // 2. create user, branch obj and save that in  db
        User user = new User();
        user.setUsername(executiveDto.username());
        user.setPassword(passwordEncoder.encode(executiveDto.password()));
        user.setRole(Role.EXECUTIVE);
        userRepository.save(user);

        Branch branch = branchRepository.findById(executiveDto.branchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

        // 3. create customer obj and map the details from customerDto
        Executive executive = ExecutiveMapper.convertDtoToExecutive(executiveDto);

        // 4. add user, branch obj into customer obj
        executive.setUser(user);
        executive.setBranch(branch);

        // 5. save customer obj in db
        executiveRepository.save(executive);
    }

    public void delete(Long executiveId, String loggedUsername) {
        // 1. check the logged-in user is admin or not
        roleCheckUtility.checkAdmin(loggedUsername);

        // 2. get customer by id
        Executive executive = executiveRepository.findById(executiveId)
                .orElseThrow(() -> new ResourceNotFoundException("Executive id not found"));
        // 3. set user active as false to softDelete
        executive.getUser().setActive(false);
        // 4. save in db
        executiveRepository.save(executive);
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

    public List<ExecutiveInfoDto> dynamicCustomerFilter(String name, JobTitle jobTitle, String username, LocalDate createdFrom, LocalDate createdTo, int page, int size) {
        // 1. Set page and size
        Pageable pageable = PageRequest.of(page, size);

        // 2. get the list from repo and return
        return executiveRepository.dynamicExecutiveFilter(name, jobTitle, username, createdFrom, createdTo, pageable);
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
    }
}
