package com.springboot.BankingSystem.model;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.springboot.BankingSystem.enums.LoanType;
import com.springboot.BankingSystem.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

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

    @Column(name = "transaction_type")
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Column(name = "designation_acc_number", nullable = false)
    private Long designationAccountNumber;

    private double amount;

    @Column(name = "time_stamp")
    @CreationTimestamp
    private Instant timeStamp;

    @Column(name = "is_completed")
    private boolean isCompleted;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
}
