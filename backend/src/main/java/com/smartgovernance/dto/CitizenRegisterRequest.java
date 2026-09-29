package com.smartgovernance.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for Citizen Registration request with comprehensive field validations.
 */
public class CitizenRegisterRequest {

    @NotBlank(message = "Full Name is required")
    @Size(max = 100, message = "Full Name cannot exceed 100 characters")
    private String fullName;

    @NotBlank(message = "Aadhaar Number is required")
    @Pattern(regexp = "^[0-9]{12}$", message = "Aadhaar Number must be exactly 12 numeric digits")
    private String aadhaarNumber;

    @NotBlank(message = "Email address is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Phone number must be a valid 10-digit Indian mobile number")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotBlank(message = "Confirm Password is required")
    private String confirmPassword;

    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;

    public CitizenRegisterRequest() {
    }

    public CitizenRegisterRequest(String fullName, String aadhaarNumber, String email, String phoneNumber, String password, String confirmPassword, String address) {
        this.fullName = fullName;
        this.aadhaarNumber = aadhaarNumber;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.address = address;
    }

    // Getters and Setters
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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
