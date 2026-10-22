package com.springboot.BankingSystem.model;

import com.springboot.BankingSystem.enums.LoanStatus;
import com.springboot.BankingSystem.enums.LoanType;
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
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "loan_type")
    @Enumerated(EnumType.STRING)
    private LoanType loanType;

    private BigDecimal amount;

    @Column(name = "disbursed_date")
    private LocalDate disbursedDate;

    @Column(name = "interest_rate", nullable = false)
    private BigDecimal interestRate;

    @Column(name = "tenure_months", nullable = false)
    private int tenureMonths;

    @Column(name = "loan_status")
    @Enumerated(EnumType.STRING)
    private LoanStatus loanStatus = LoanStatus.IN_PROCESS;

    @Column(name = "requested_on")
    @CreationTimestamp
    private LocalDate requestedOn;

    @Column(name = "processed_on")
    private LocalDate processedOn;

    private String remark;

    @Column(name = "outstanding_amount")
    private BigDecimal outstandingAmount;

    @Column(name = "closed_on")
    private LocalDate closedOn;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "executive_id")
    private Executive executive;
}
