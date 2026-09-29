package com.smartgovernance.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Citizen Entity representing registered citizens in the platform.
 */
@Entity
@Table(name = "citizen", uniqueConstraints = {
    @UniqueConstraint(name = "uq_citizen_aadhaar", columnNames = "aadhaar_number"),
    @UniqueConstraint(name = "uq_citizen_email", columnNames = "email")
})
public class Citizen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "citizen_id")
    private Long citizenId;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "aadhaar_number", nullable = false, length = 12, unique = true)
    private String aadhaarNumber;

    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;

    @Column(name = "phone_number", nullable = false, length = 15)
    private String phoneNumber;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Citizen() {
    }

    public Citizen(String fullName, String aadhaarNumber, String email, String phoneNumber, String password, String address) {
        this.fullName = fullName;
        this.aadhaarNumber = aadhaarNumber;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.address = address;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getCitizenId() {
        return citizenId;
    }

    public void setCitizenId(Long citizenId) {
        this.citizenId = citizenId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAadhaarNumber() {
        return aadhaarNumber;
    }

    public void setAadhaarNumber(String aadhaarNumber) {
        this.aadhaarNumber = aadhaarNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
