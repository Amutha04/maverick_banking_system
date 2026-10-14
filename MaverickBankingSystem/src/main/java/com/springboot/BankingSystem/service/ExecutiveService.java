package com.springboot.BankingSystem.service;

import com.springboot.BankingSystem.dto.request.ExecutiveDto;
import com.springboot.BankingSystem.dto.response.ExecutiveInfoDto;
import com.springboot.BankingSystem.enums.JobTitle;
import com.springboot.BankingSystem.enums.Role;
import com.springboot.BankingSystem.exception.ResourceNotFoundException;
import com.springboot.BankingSystem.mapper.ExecutiveMapper;
import com.springboot.BankingSystem.model.Executive;
import com.springboot.BankingSystem.model.User;
import com.springboot.BankingSystem.repository.ExecutiveRepository;
import com.springboot.BankingSystem.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExecutiveService {
    private final ExecutiveRepository executiveRepository;
    private final UserRepository userRepository;

    public void add(@Valid ExecutiveDto executiveDto) {
        // 1. create user obj and save that in  db
        User user = new User();
        user.setUsername(executiveDto.username());
        user.setPassword(executiveDto.password());
        user.setRole(Role.EXECUTIVE);
        userRepository.save(user);

        // 2. create customer obj and map the details from customerDto
        Executive executive = ExecutiveMapper.convertDtoToExecutive(executiveDto);

        // 3. add user obj into customer obj
        executive.setUser(user);

        // 4. save customer obj in db
        executiveRepository.save(executive);
    }

    public void delete(Long executiveId) {
        // 1. get customer by id
        Executive executive = executiveRepository.findById(executiveId)
                .orElseThrow(() -> new ResourceNotFoundException("Executive id not found"));
        // 2. set user active as false to softDelete
        executive.getUser().setActive(false);
        // 3. save in db
        executiveRepository.save(executive);
    }

    public List<ExecutiveInfoDto> getAll(int page, int size) {
        // 1. Set page and size
        Pageable pageable = PageRequest.of(page, size);
        //2. get all customers
        return executiveRepository.getAll(pageable);
    }

    public ExecutiveInfoDto getById(Long id) {
        Executive executive = executiveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Executive id not found"));
        return ExecutiveMapper.convertExecutiveToDto(executive);
    }

    public List<ExecutiveInfoDto> dynamicCustomerFilter(String name, JobTitle jobTitle, String username, LocalDate createdFrom, LocalDate createdTo, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return executiveRepository.dynamicExecutiveFilter(name, jobTitle, username, createdFrom, createdTo, pageable);
    }
}
