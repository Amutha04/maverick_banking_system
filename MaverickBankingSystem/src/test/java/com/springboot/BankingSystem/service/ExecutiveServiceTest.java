package com.springboot.BankingSystem.service;

import com.springboot.BankingSystem.dto.request.ExecutiveDto;
import com.springboot.BankingSystem.dto.response.ExecutiveInfoDto;
import com.springboot.BankingSystem.enums.BranchName;
import com.springboot.BankingSystem.enums.JobTitle;
import com.springboot.BankingSystem.exception.DuplicateRequestException;
import com.springboot.BankingSystem.exception.InvalidAccountStateException;
import com.springboot.BankingSystem.exception.OwnershipException;
import com.springboot.BankingSystem.exception.ResourceNotFoundException;
import com.springboot.BankingSystem.model.Branch;
import com.springboot.BankingSystem.model.Executive;
import com.springboot.BankingSystem.model.User;
import com.springboot.BankingSystem.repository.AccountRequestRepository;
import com.springboot.BankingSystem.repository.BranchRepository;
import com.springboot.BankingSystem.repository.ExecutiveRepository;
import com.springboot.BankingSystem.repository.LoanRepository;
import com.springboot.BankingSystem.repository.UserRepository;
import com.springboot.BankingSystem.utility.RoleCheckUtility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExecutiveServiceTest {
    @InjectMocks
    private ExecutiveService executiveService;
    @Mock
    private ExecutiveRepository executiveRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BranchRepository branchRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RoleCheckUtility roleCheckUtility;
    @Mock
    private AccountRequestRepository accountRequestRepository;
    @Mock
    private LoanRepository loanRepository;

    @Test
    public void addTest() {
        ExecutiveDto executiveDto = new ExecutiveDto("Arjun Mehta", "arjun@gmail.com", 9876500001L, JobTitle.PERSONAL_BANKER, "arjun", "arjun@123", 1L);
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setName(BranchName.ANNA_NAGAR);

        when(userRepository.existsByUsername("arjun")).thenReturn(false);
        when(passwordEncoder.encode("arjun@123")).thenReturn("encodedPassword");
        when(branchRepository.findById(1L)).thenReturn(Optional.of(branch));

        executiveService.add(executiveDto, "admin");

        verify(roleCheckUtility, times(1)).checkAdmin("admin");
        verify(userRepository, times(1)).existsByUsername("arjun");
        verify(passwordEncoder, times(1)).encode("arjun@123");
        verify(userRepository, times(1)).save(any(User.class));
        verify(branchRepository, times(1)).findById(1L);
        verify(executiveRepository, times(1)).save(any(Executive.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(executiveRepository);
    }

    @Test
    public void addTest_Invalid() {
        // username is already taken
        ExecutiveDto executiveDto = new ExecutiveDto("Arjun Mehta", "arjun@gmail.com", 9876500001L, JobTitle.PERSONAL_BANKER, "arjun", "arjun@123", 1L);

        when(userRepository.existsByUsername("arjun")).thenReturn(true);

        assertThrows(DuplicateRequestException.class, () -> executiveService.add(executiveDto, "admin"));

        verify(roleCheckUtility, times(1)).checkAdmin("admin");
        verify(userRepository, times(1)).existsByUsername("arjun");
        verify(passwordEncoder, times(0)).encode("arjun@123");
        verify(userRepository, times(0)).save(any(User.class));
        verify(branchRepository, times(0)).findById(1L);
        verify(executiveRepository, times(0)).save(any(Executive.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(executiveRepository);
    }

    @Test
    public void addTest_BranchNotFound() {
        ExecutiveDto executiveDto = new ExecutiveDto("Arjun Mehta", "arjun@gmail.com", 9876500001L, JobTitle.PERSONAL_BANKER, "arjun", "arjun@123", 5L);

        when(userRepository.existsByUsername("arjun")).thenReturn(false);
        when(passwordEncoder.encode("arjun@123")).thenReturn("encodedPassword");
        when(branchRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> executiveService.add(executiveDto, "admin"));

        // the user is saved before the branch lookup, @Transactional rolls it back
        verify(roleCheckUtility, times(1)).checkAdmin("admin");
        verify(userRepository, times(1)).existsByUsername("arjun");
        verify(passwordEncoder, times(1)).encode("arjun@123");
        verify(userRepository, times(1)).save(any(User.class));
        verify(branchRepository, times(1)).findById(5L);
        verify(executiveRepository, times(0)).save(any(Executive.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(executiveRepository);
    }

    @Test
    public void addTest_NotAdmin() {
        ExecutiveDto executiveDto = new ExecutiveDto("Arjun Mehta", "arjun@gmail.com", 9876500001L, JobTitle.PERSONAL_BANKER, "arjun", "arjun@123", 1L);

        when(roleCheckUtility.checkAdmin("ravi")).thenThrow(OwnershipException.class);

        assertThrows(OwnershipException.class, () -> executiveService.add(executiveDto, "ravi"));

        verify(roleCheckUtility, times(1)).checkAdmin("ravi");
        verify(userRepository, times(0)).existsByUsername("arjun");
        verify(userRepository, times(0)).save(any(User.class));
        verify(executiveRepository, times(0)).save(any(Executive.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(branchRepository);
        verifyNoMoreInteractions(executiveRepository);
    }

    @Test
    public void deleteTest() {
        User user = new User();
        Executive executive = new Executive();
        executive.setId(1L);
        executive.setUser(user);

        when(executiveRepository.findById(1L)).thenReturn(Optional.of(executive));
        when(accountRequestRepository.getPendingCountByExecutive(1L)).thenReturn(0L);
        when(loanRepository.getPendingCountByExecutive(1L)).thenReturn(0L);

        executiveService.delete(1L, "admin");

        verify(roleCheckUtility, times(1)).checkAdmin("admin");
        verify(executiveRepository, times(1)).findById(1L);
        verify(accountRequestRepository, times(1)).getPendingCountByExecutive(1L);
        verify(loanRepository, times(1)).getPendingCountByExecutive(1L);
        verify(executiveRepository, times(1)).save(executive);

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(accountRequestRepository);
        verifyNoMoreInteractions(loanRepository);
    }

    @Test
    public void deleteTest_Invalid() {
        when(executiveRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> executiveService.delete(5L, "admin"));

        verify(roleCheckUtility, times(1)).checkAdmin("admin");
        verify(executiveRepository, times(1)).findById(5L);
        verify(accountRequestRepository, times(0)).getPendingCountByExecutive(5L);
        verify(loanRepository, times(0)).getPendingCountByExecutive(5L);
        verify(executiveRepository, times(0)).save(any(Executive.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(accountRequestRepository);
        verifyNoMoreInteractions(loanRepository);
    }

    @Test
    public void deleteTest_PendingWork() {
        // executive still holds pending account requests, so cannot be deleted
        Executive executive = new Executive();
        executive.setId(1L);

        when(executiveRepository.findById(1L)).thenReturn(Optional.of(executive));
        when(accountRequestRepository.getPendingCountByExecutive(1L)).thenReturn(2L);
        when(loanRepository.getPendingCountByExecutive(1L)).thenReturn(0L);

        assertThrows(InvalidAccountStateException.class, () -> executiveService.delete(1L, "admin"));

        verify(roleCheckUtility, times(1)).checkAdmin("admin");
        verify(executiveRepository, times(1)).findById(1L);
        verify(accountRequestRepository, times(1)).getPendingCountByExecutive(1L);
        verify(loanRepository, times(1)).getPendingCountByExecutive(1L);
        verify(executiveRepository, times(0)).save(any(Executive.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(accountRequestRepository);
        verifyNoMoreInteractions(loanRepository);
    }

    @Test
    public void getAllTest() {
        ExecutiveInfoDto dto1 = new ExecutiveInfoDto(1L, "Arjun Mehta", "arjun@gmail.com", 9876500001L, JobTitle.PERSONAL_BANKER, Instant.now(), "arjun", BranchName.ANNA_NAGAR);
        ExecutiveInfoDto dto2 = new ExecutiveInfoDto(2L, "Meena Iyer", "meena@gmail.com", 9876500002L, JobTitle.LOAN_OFFICER, Instant.now(), "meena", BranchName.T_NAGAR);

        when(executiveRepository.getAll(PageRequest.of(0, 5))).thenReturn(List.of(dto1, dto2));

        assertEquals(2, executiveService.getAll("admin", 0, 5).size());

        verify(roleCheckUtility, times(1)).checkAdmin("admin");
        verify(executiveRepository, times(1)).getAll(PageRequest.of(0, 5));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(executiveRepository);
    }

    @Test
    public void getByIdTest() {
        User user = new User();
        user.setUsername("arjun");
        Branch branch = new Branch();
        branch.setName(BranchName.ANNA_NAGAR);
        Executive executive = new Executive();
        executive.setId(1L);
        executive.setUser(user);
        executive.setBranch(branch);

        when(executiveRepository.findById(1L)).thenReturn(Optional.of(executive));

        assertNotNull(executiveService.getById("admin", 1L));

        verify(roleCheckUtility, times(1)).checkAdmin("admin");
        verify(executiveRepository, times(1)).findById(1L);

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(executiveRepository);
    }

    @Test
    public void getByIdTest_Invalid() {
        when(executiveRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> executiveService.getById("admin", 5L));

        verify(roleCheckUtility, times(1)).checkAdmin("admin");
        verify(executiveRepository, times(1)).findById(5L);

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(executiveRepository);
    }

    @Test
    public void dynamicExecutiveFilterTest() {
        ExecutiveInfoDto dto1 = new ExecutiveInfoDto(1L, "Arjun Mehta", "arjun@gmail.com", 9876500001L, JobTitle.PERSONAL_BANKER, Instant.now(), "arjun", BranchName.ANNA_NAGAR);

        // the repository gives back a page, so wrap the list in PageImpl
        when(executiveRepository.dynamicExecutiveFilter(null, JobTitle.PERSONAL_BANKER, null, null, null, null, PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(dto1), PageRequest.of(0, 5), 1));

        assertEquals(1, executiveService.dynamicExecutiveFilter(null, JobTitle.PERSONAL_BANKER, null, null, null, null, 0, 5, "admin").list().size());

        verify(roleCheckUtility, times(1)).checkAdmin("admin");
        verify(executiveRepository, times(1)).dynamicExecutiveFilter(null, JobTitle.PERSONAL_BANKER, null, null, null, null, PageRequest.of(0, 5));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(executiveRepository);
    }

    @Test
    public void getTest() {
        User user = new User();
        user.setUsername("arjun");
        Branch branch = new Branch();
        branch.setName(BranchName.ANNA_NAGAR);
        Executive executive = new Executive();
        executive.setId(1L);
        executive.setUser(user);
        executive.setBranch(branch);

        when(executiveRepository.findByUsername("arjun")).thenReturn(executive);

        assertNotNull(executiveService.get("arjun"));

        verify(executiveRepository, times(1)).findByUsername("arjun");

        verifyNoMoreInteractions(executiveRepository);
    }

    @Test
    public void updateJobTitleTest() {
        Executive executive = new Executive();
        executive.setId(1L);

        when(executiveRepository.findById(1L)).thenReturn(Optional.of(executive));

        executiveService.updateJobTitle(1L, JobTitle.LOAN_OFFICER, "admin");

        verify(roleCheckUtility, times(1)).checkAdmin("admin");
        verify(executiveRepository, times(1)).findById(1L);
        verify(executiveRepository, times(1)).save(executive);

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(executiveRepository);
    }

    @Test
    public void updateJobTitleTest_Invalid() {
        when(executiveRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> executiveService.updateJobTitle(5L, JobTitle.LOAN_OFFICER, "admin"));

        verify(roleCheckUtility, times(1)).checkAdmin("admin");
        verify(executiveRepository, times(1)).findById(5L);
        verify(executiveRepository, times(0)).save(any(Executive.class));

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(executiveRepository);
    }

    @Test
    public void getTeamTest() {
        // manager sees the executives of the own branch
        Branch branch = new Branch();
        branch.setId(1L);
        Executive manager = new Executive();
        manager.setId(1L);
        manager.setBranch(branch);
        Executive executive1 = new Executive();
        executive1.setId(2L);
        executive1.setName("Arjun Mehta");
        Executive executive2 = new Executive();
        executive2.setId(3L);
        executive2.setName("Meena Iyer");

        when(executiveRepository.findByUsername("manager")).thenReturn(manager);
        when(executiveRepository.getByBranch(1L)).thenReturn(List.of(executive1, executive2));

        assertEquals(2, executiveService.getTeam("manager").size());

        verify(roleCheckUtility, times(1)).checkManager("manager");
        verify(executiveRepository, times(1)).findByUsername("manager");
        verify(executiveRepository, times(1)).getByBranch(1L);
        verify(accountRequestRepository, times(1)).getPendingCountByExecutive(2L);
        verify(loanRepository, times(1)).getPendingCountByExecutive(2L);
        verify(accountRequestRepository, times(1)).getPendingCountByExecutive(3L);
        verify(loanRepository, times(1)).getPendingCountByExecutive(3L);

        verifyNoMoreInteractions(roleCheckUtility);
        verifyNoMoreInteractions(executiveRepository);
        verifyNoMoreInteractions(accountRequestRepository);
        verifyNoMoreInteractions(loanRepository);
    }
}
