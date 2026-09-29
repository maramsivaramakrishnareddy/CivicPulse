package com.smartgovernance.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Officer Entity representing administrative personnel provisioned by administration.
 */
@Entity
@Table(name = "officer", uniqueConstraints = {
    @UniqueConstraint(name = "uq_officer_employee_id", columnNames = "employee_id"),
    @UniqueConstraint(name = "uq_officer_email", columnNames = "email")
})
public class Officer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "officer_id")
    private Long officerId;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "employee_id", nullable = false, length = 50, unique = true)
    private String employeeId;

    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;

    @Column(name = "phone_number", nullable = false, length = 15)
    private String phoneNumber;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Officer() {
    }

    public Officer(String fullName, String employeeId, String department, String email, String phoneNumber, String password) {
        this.fullName = fullName;
        this.employeeId = employeeId;
        this.department = department;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
