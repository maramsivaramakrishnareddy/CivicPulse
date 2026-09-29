package com.smartgovernance.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Officer Module DTO: request body for updating a complaint's status and remarks.
 * Used by PUT /api/officer/my-complaints/{complaintId}/status.
 */
public class OfficerComplaintUpdateRequest {

    @NotBlank(message = "Status is required")
    private String status;

    private String remarks;

    public OfficerComplaintUpdateRequest() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
