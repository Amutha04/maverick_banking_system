package com.springboot.BankingSystem.controller;

import com.springboot.BankingSystem.dto.request.ExecutiveDto;
import com.springboot.BankingSystem.dto.response.CustomerRespDto;
import com.springboot.BankingSystem.dto.response.ExecutiveInfoDto;
import com.springboot.BankingSystem.enums.JobTitle;
import com.springboot.BankingSystem.service.ExecutiveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ExecutiveController {

    private final ExecutiveService executiveService;

    @PostMapping("/api/executive/add")
    public void add(@Valid @RequestBody ExecutiveDto executiveDto,
                    Principal principal) {
        executiveService.add(executiveDto, principal.getName());
    }

    @PutMapping("/api/executive/delete/{executiveId}")
    public void delete(@PathVariable("executiveId") Long executiveId,
                       Principal principal) {
        executiveService.delete(executiveId, principal.getName());
    }

    @GetMapping("/api/executive/all")
    public List<ExecutiveInfoDto> getAll(@RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                         @RequestParam(name = "size", required = false, defaultValue = "20") int size,
                                         Principal principal) {
        return executiveService.getAll(principal.getName(), page, size);
    }

    @GetMapping("/api/executive/{executiveId}")
    public ExecutiveInfoDto getByID(@PathVariable("executiveId") Long id,
                                    Principal principal) {
        return executiveService.getById(principal.getName(),id);
    }

    @GetMapping("/api/executive")
    public ExecutiveInfoDto get(Principal principal) {
        return executiveService.get(principal.getName());
    }

    @PutMapping("/api/executive/update-jobtitle/{executiveId}")
    public void updateJobTitle(@PathVariable long executiveId,
                               @RequestParam("jobTitle") JobTitle jobTitle,
                               Principal principal) {
        executiveService.updateJobTitle(executiveId, jobTitle, principal.getName());
    }

    @GetMapping("/api/executive/filter")
    public List<ExecutiveInfoDto> dynamicExecutiveFilter(@RequestParam(name = "name", required = false) String name,
                                                       @RequestParam(name = "jobTitle", required = false) JobTitle jobTitle,
                                                       @RequestParam(name = "username", required = false) String username,
                                                       @RequestParam(name = "createdFrom", required = false) LocalDate createdFrom,
                                                       @RequestParam(name = "createdTo", required = false) LocalDate createdTo,
                                                       @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                                       @RequestParam(name = "size", required = false, defaultValue = "20") int size) {
        return executiveService.dynamicCustomerFilter(name, jobTitle, username, createdFrom, createdTo, page, size);

    }
}
