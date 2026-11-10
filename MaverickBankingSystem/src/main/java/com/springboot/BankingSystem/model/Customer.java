package com.springboot.BankingSystem.model;

import com.springboot.BankingSystem.enums.Gender;
import com.springboot.BankingSystem.enums.KYCStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false)
    private LocalDate dob;

    @Column(length = 1000)
    private String address;

    private String email;

    @Column(nullable = false)
    private Long contact;

    @Column(name = "aadhaar_number")
    private Long aadhaarNumber;

    @Column(name = "pan_number")
    private String panNumber;

    @Column(name = "aadhaar_url")
    private String aadhaarUrl;

    @Column(name = "pan_url")
    private String panUrl;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "signature_url")
    private String signatureUrl;

    @Column(name = "cibil_score")
    private Integer cibilScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status")
    private KYCStatus kycStatus = KYCStatus.PENDING;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    public void init() {
        cibilScore = (int) (Math.random() * (601)) + 300;;
    }
}
