package com.smartgovernance.dto;

import jakarta.validation.constraints.NotBlank;

public class ComplaintDuplicateStatusRequest {

    @NotBlank(message = "Duplicate status is required")
    private String duplicateStatus;

    private String notes;

    private String originalComplaintId;

    public ComplaintDuplicateStatusRequest() {
    }

    public String getDuplicateStatus() {
        return duplicateStatus;
    }

    public void setDuplicateStatus(String duplicateStatus) {
        this.duplicateStatus = duplicateStatus;
    }

    public String getOriginalComplaintId() {
        return originalComplaintId;
    }

    public void setOriginalComplaintId(String originalComplaintId) {
        this.originalComplaintId = originalComplaintId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
