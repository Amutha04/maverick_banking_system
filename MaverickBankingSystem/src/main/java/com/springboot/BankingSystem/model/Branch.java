package com.springboot.BankingSystem.model;

import com.springboot.BankingSystem.enums.BranchName;
import com.springboot.BankingSystem.enums.District;
import com.springboot.BankingSystem.enums.IFSC;
import com.springboot.BankingSystem.enums.State;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class Branch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private BranchName name;

    @Enumerated(EnumType.STRING)
    private State state;

    @Enumerated(EnumType.STRING)
    private District district;

    @Enumerated(EnumType.STRING)
    @Column(name = "ifsc_code")
    private IFSC ifscCode;

}
