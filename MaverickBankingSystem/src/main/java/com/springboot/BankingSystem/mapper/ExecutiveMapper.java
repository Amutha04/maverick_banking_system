package com.springboot.BankingSystem.mapper;

import com.springboot.BankingSystem.dto.request.ExecutiveDto;
import com.springboot.BankingSystem.dto.response.ExecutiveInfoDto;
import com.springboot.BankingSystem.model.Executive;
import org.springframework.stereotype.Component;

@Component
public class ExecutiveMapper {
    public static Executive convertDtoToExecutive(ExecutiveDto executiveDto) {
        Executive executive = new Executive();
        executive.setName(executiveDto.name());
        executive.setEmail(executiveDto.email());
        executive.setContact(executiveDto.contact());
        executive.setJobTitle(executiveDto.jobTitle());
        return executive;
    }

    public static ExecutiveInfoDto convertExecutiveToDto(Executive executive) {
        return new ExecutiveInfoDto(
                executive.getName(),
                executive.getEmail(),
                executive.getContact(),
                executive.getJobTitle(),
                executive.getUpdatedAt(),
                executive.getUser().getUsername()
        );
    }
}
