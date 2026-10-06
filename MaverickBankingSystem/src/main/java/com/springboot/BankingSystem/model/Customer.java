package com.springboot.BankingSystem.model;

import com.springboot.BankingSystem.enums.JobTitle;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name;

    private long phoneNumber;

    @Column(name = "job_title")
    @Enumerated(EnumType.STRING)
    private JobTitle jobTitle;
}
