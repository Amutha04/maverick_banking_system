package com.springboot.BankingSystem.model;

import com.springboot.BankingSystem.enums.AccountType;
import com.springboot.BankingSystem.enums.RequestStatus;
import com.springboot.BankingSystem.enums.RequestType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class AccountRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private RequestType requestType;

    @Column(name = "account_type")
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Column(name = "request_status")
    @Enumerated(EnumType.STRING)
    private RequestStatus requestStatus = RequestStatus.IN_PROCESS;

    private String reason;

    @Column(name = "initial_deposit")
    private BigDecimal initialDeposit;

    @Column(name = "requested_on")
    @CreationTimestamp
    private LocalDate requestedOn;

    @Column(name = "processed_on")
    private LocalDate processedOn;

    private String remark;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "executive_id")
    private Executive executive;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;
}
