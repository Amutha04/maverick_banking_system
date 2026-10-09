package com.springboot.BankingSystem.model;

import com.springboot.BankingSystem.enums.LoanStatus;
import com.springboot.BankingSystem.enums.LoanType;
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
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "loan_type")
    @Enumerated(EnumType.STRING)
    private LoanType loanType;

    private double amount;

    @Column(name = "disbursed_date")
    @CreationTimestamp
    private Instant disbursedDate;

    @Column(name = "interest_rate", nullable = false)
    private int interestRate;

    @Column(name = "tenure_months", nullable = false)
    private int tenureMonths;

    @Column(name = "loan_status")
    @Enumerated(EnumType.STRING)
    private LoanStatus loanStatus;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "executive_id")
    private Executive executive;
}
