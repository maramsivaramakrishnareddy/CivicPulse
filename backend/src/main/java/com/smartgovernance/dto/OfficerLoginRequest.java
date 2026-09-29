package com.smartgovernance.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for Officer Login credentials.
 */
public class OfficerLoginRequest {

    @NotBlank(message = "Employee ID is required")
    private String employeeId;

    @NotBlank(message = "Password is required")
    private String password;

    public OfficerLoginRequest() {
    }

    public OfficerLoginRequest(String employeeId, String password) {
        this.employeeId = employeeId;
        this.password = password;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
