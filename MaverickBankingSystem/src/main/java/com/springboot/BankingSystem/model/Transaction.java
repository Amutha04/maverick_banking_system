package com.springboot.BankingSystem.model;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.springboot.BankingSystem.enums.LoanType;
import com.springboot.BankingSystem.enums.TransactionStatus;
import com.springboot.BankingSystem.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_number", nullable = false, updatable = false)
    private String referenceNumber;

    @Column(name = "transaction_type")
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Column(name = "transaction_status")
    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    private BigDecimal amount;

    @Column(name = "destination_account", nullable = false)
    private Long destinationAccount;

    private BigDecimal balance;

    @Column(name = "time_stamp")
    @CreationTimestamp
    private LocalDate timeStamp;

    private String remarks;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
}
