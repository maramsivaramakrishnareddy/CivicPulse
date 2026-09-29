package com.smartgovernance.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for Citizen profile info (safe, excluding password).
 */
public class CitizenResponseDto {

    private Long citizenId;
    private String fullName;
    private String aadhaarNumber;
    private String email;
    private String phoneNumber;
    private String address;
    private LocalDateTime createdAt;
    private String userType;

    public CitizenResponseDto() {
        this.userType = "CITIZEN";
    }

    public CitizenResponseDto(Long citizenId, String fullName, String aadhaarNumber, String email, String phoneNumber, String address, LocalDateTime createdAt) {
        this.citizenId = citizenId;
        this.fullName = fullName;
        this.aadhaarNumber = aadhaarNumber;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.createdAt = createdAt;
        this.userType = "CITIZEN";
    }

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

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
