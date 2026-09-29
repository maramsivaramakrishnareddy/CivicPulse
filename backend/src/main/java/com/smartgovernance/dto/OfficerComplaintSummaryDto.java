package com.smartgovernance.dto;

import java.time.LocalDateTime;

/**
 * Officer Module DTO: summary row for a complaint assigned to the logged-in officer.
 * Used by GET /api/officer/my-complaints and the dashboard's "recent complaints" list.
 */
public class OfficerComplaintSummaryDto {

    private String complaintId;
    private String title;
    private String departmentName;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String complaintType;
    private String location;
    private Double latitude;
    private Double longitude;
    private String aiPriority;
    private Boolean aiPhotoMismatchSuspected;
    private String aiPhotoMismatchReason;
    private String duplicateStatus;
    private String possibleDuplicateComplaintId;
    private Double textSimilarity;

    public OfficerComplaintSummaryDto() {
    }

    public OfficerComplaintSummaryDto(String complaintId, String title, String departmentName,
                                       String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.complaintId = complaintId;
        this.title = title;
        this.departmentName = departmentName;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(String complaintId) {
        this.complaintId = complaintId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getComplaintType() { return complaintType; }
    public void setComplaintType(String complaintType) { this.complaintType = complaintType; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getAiPriority() { return aiPriority; }
    public void setAiPriority(String aiPriority) { this.aiPriority = aiPriority; }
    public Boolean getAiPhotoMismatchSuspected() { return aiPhotoMismatchSuspected; }
    public void setAiPhotoMismatchSuspected(Boolean value) { this.aiPhotoMismatchSuspected = value; }
    public String getAiPhotoMismatchReason() { return aiPhotoMismatchReason; }
    public void setAiPhotoMismatchReason(String value) { this.aiPhotoMismatchReason = value; }

    public String getDuplicateStatus() { return duplicateStatus; }
    public void setDuplicateStatus(String duplicateStatus) { this.duplicateStatus = duplicateStatus; }
    public String getPossibleDuplicateComplaintId() { return possibleDuplicateComplaintId; }
    public void setPossibleDuplicateComplaintId(String possibleDuplicateComplaintId) { this.possibleDuplicateComplaintId = possibleDuplicateComplaintId; }
    public Double getTextSimilarity() { return textSimilarity; }
    public void setTextSimilarity(Double textSimilarity) { this.textSimilarity = textSimilarity; }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
