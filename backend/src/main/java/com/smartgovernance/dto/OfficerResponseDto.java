package com.smartgovernance.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for Officer profile info (safe, excluding password).
 */
public class OfficerResponseDto {

    private Long officerId;
    private String fullName;
    private String employeeId;
    private String department;
    private String email;
    private String phoneNumber;
    private LocalDateTime createdAt;
    private String userType;

    public OfficerResponseDto() {
        this.userType = "OFFICER";
    }

    public OfficerResponseDto(Long officerId, String fullName, String employeeId, String department, String email, String phoneNumber, LocalDateTime createdAt) {
        this.officerId = officerId;
        this.fullName = fullName;
        this.employeeId = employeeId;
        this.department = department;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.createdAt = createdAt;
        this.userType = "OFFICER";
    }

    public Long getOfficerId() {
        return officerId;
    }

    public void setOfficerId(Long officerId) {
        this.officerId = officerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
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
