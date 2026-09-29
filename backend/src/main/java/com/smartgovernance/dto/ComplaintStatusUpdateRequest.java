package com.smartgovernance.dto;

import jakarta.validation.constraints.NotBlank;

public class ComplaintStatusUpdateRequest {

    @NotBlank(message = "Status is required")
    private String status;

    private String actionTaken;

    public ComplaintStatusUpdateRequest() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }
}
