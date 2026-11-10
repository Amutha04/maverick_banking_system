package com.springboot.BankingSystem.model;

import com.springboot.BankingSystem.enums.BranchName;
import com.springboot.BankingSystem.enums.IFSC;
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
public class Beneficiaries {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "account_number", nullable = false)
    private Long accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "branch_name", nullable = false)
    private BranchName branchName;

    @Enumerated(EnumType.STRING)
    @Column(name = "ifsc_code", nullable = false)
    private IFSC ifscCode;

    @Column(name = "added_on")
    @CreationTimestamp
    private Instant addedOn;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

}
