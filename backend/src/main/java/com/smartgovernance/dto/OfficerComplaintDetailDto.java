package com.smartgovernance.dto;

import java.time.LocalDateTime;

/**
 * Officer Module DTO: full complaint detail for the Complaint Review page.
 * Used by GET /api/officer/my-complaints/{complaintId}.
 */
public class OfficerComplaintDetailDto {

    private String complaintId;
    private String title;
    private String description;
    private String departmentName;
    private Long departmentId;
    private String location;
    private String status;
    private String remarks;
    private String citizenName;
    private Long citizenId;
    private String citizenEmail;
    private String citizenPhone;
    private String complaintType;
    private String imageUrl;
    private String videoUrl;
    private Double latitude;
    private Double longitude;
    private String duplicateStatus;
    private String duplicateOfComplaintId;
    private String possibleDuplicateComplaintId;
    private Double textSimilarity;
    private Double distanceMetres;
    private OfficerReopenRequestDto reopenRequest;
    private OfficerEscalationDetailDto latestEscalation;
    private FeedbackDto feedback;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public String getCitizenName() {
        return citizenName;
    }

    public void setCitizenName(String citizenName) {
        this.citizenName = citizenName;
    }

    public Long getCitizenId() { return citizenId; }
    public void setCitizenId(Long citizenId) { this.citizenId = citizenId; }
    public String getCitizenEmail() { return citizenEmail; }
    public void setCitizenEmail(String citizenEmail) { this.citizenEmail = citizenEmail; }
    public String getCitizenPhone() { return citizenPhone; }
    public void setCitizenPhone(String citizenPhone) { this.citizenPhone = citizenPhone; }
    public String getComplaintType() { return complaintType; }
    public void setComplaintType(String complaintType) { this.complaintType = complaintType; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getDuplicateStatus() { return duplicateStatus; }
    public void setDuplicateStatus(String duplicateStatus) { this.duplicateStatus = duplicateStatus; }
    public String getDuplicateOfComplaintId() { return duplicateOfComplaintId; }
    public void setDuplicateOfComplaintId(String duplicateOfComplaintId) { this.duplicateOfComplaintId = duplicateOfComplaintId; }
    public String getPossibleDuplicateComplaintId() { return possibleDuplicateComplaintId; }
    public void setPossibleDuplicateComplaintId(String possibleDuplicateComplaintId) { this.possibleDuplicateComplaintId = possibleDuplicateComplaintId; }
    public Double getTextSimilarity() { return textSimilarity; }
    public void setTextSimilarity(Double textSimilarity) { this.textSimilarity = textSimilarity; }
    public Double getDistanceMetres() { return distanceMetres; }
    public void setDistanceMetres(Double distanceMetres) { this.distanceMetres = distanceMetres; }
    public OfficerReopenRequestDto getReopenRequest() { return reopenRequest; }
    public void setReopenRequest(OfficerReopenRequestDto reopenRequest) { this.reopenRequest = reopenRequest; }
    public OfficerEscalationDetailDto getLatestEscalation() { return latestEscalation; }
    public void setLatestEscalation(OfficerEscalationDetailDto latestEscalation) { this.latestEscalation = latestEscalation; }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public FeedbackDto getFeedback() {
        return feedback;
    }

    public void setFeedback(FeedbackDto feedback) {
        this.feedback = feedback;
    }
}
