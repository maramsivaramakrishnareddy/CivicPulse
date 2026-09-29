package com.smartgovernance.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComplaintResponseDto {

    private String complaintId;
    private String title;
    private String description;
    private String departmentName;
    private String citizenName;
    private String officerName;
    private String complaintType;
    private String status;
    private String duplicateStatus;
    private String possibleDuplicateComplaintId;
    private String duplicateOfComplaintId;
    private Double textSimilarity;
    private Double distanceMetres;
    private String location;
    private String imageUrl;
    private String videoUrl;
    private Double latitude;
    private Double longitude;
    private String actionTaken;
    private String aiPriority;
    private Boolean aiPhotoMismatchSuspected;
    private String aiPhotoMismatchReason;
    private String escalationStatus;
    private String escalationReason;
    private String escalationTarget;
    private Integer escalationLevel;
    private FeedbackDto feedback;
    private Boolean hasFeedback;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;

    public ComplaintResponseDto() {
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

    public String getCitizenName() {
        return citizenName;
    }

    public void setCitizenName(String citizenName) {
        this.citizenName = citizenName;
    }

    public String getOfficerName() {
        return officerName;
    }

    public void setOfficerName(String officerName) {
        this.officerName = officerName;
    }

    public String getComplaintType() {
        return complaintType;
    }

    public void setComplaintType(String complaintType) {
        this.complaintType = complaintType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDuplicateStatus() {
        return duplicateStatus;
    }

    public void setDuplicateStatus(String duplicateStatus) {
        this.duplicateStatus = duplicateStatus;
    }

    public String getPossibleDuplicateComplaintId() {
        return possibleDuplicateComplaintId;
    }

    public void setPossibleDuplicateComplaintId(String possibleDuplicateComplaintId) {
        this.possibleDuplicateComplaintId = possibleDuplicateComplaintId;
    }

    public String getDuplicateOfComplaintId() { return duplicateOfComplaintId; }
    public void setDuplicateOfComplaintId(String duplicateOfComplaintId) { this.duplicateOfComplaintId = duplicateOfComplaintId; }

    public Double getTextSimilarity() {
        return textSimilarity;
    }

    public void setTextSimilarity(Double textSimilarity) {
        this.textSimilarity = textSimilarity;
    }

    public Double getDistanceMetres() {
        return distanceMetres;
    }

    public void setDistanceMetres(Double distanceMetres) {
        this.distanceMetres = distanceMetres;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }

    public String getAiPriority() {
        return aiPriority;
    }

    public void setAiPriority(String aiPriority) {
        this.aiPriority = aiPriority;
    }

    public Boolean getAiPhotoMismatchSuspected() {
        return aiPhotoMismatchSuspected;
    }

    public void setAiPhotoMismatchSuspected(Boolean aiPhotoMismatchSuspected) {
        this.aiPhotoMismatchSuspected = aiPhotoMismatchSuspected;
    }

    public String getAiPhotoMismatchReason() {
        return aiPhotoMismatchReason;
    }

    public void setAiPhotoMismatchReason(String aiPhotoMismatchReason) {
        this.aiPhotoMismatchReason = aiPhotoMismatchReason;
    }

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

    public String getEscalationStatus() {
        return escalationStatus;
    }

    public void setEscalationStatus(String escalationStatus) {
        this.escalationStatus = escalationStatus;
    }

    public String getEscalationReason() {
        return escalationReason;
    }

    public void setEscalationReason(String escalationReason) {
        this.escalationReason = escalationReason;
    }

    public String getEscalationTarget() {
        return escalationTarget;
    }

    public void setEscalationTarget(String escalationTarget) {
        this.escalationTarget = escalationTarget;
    }

    public Integer getEscalationLevel() {
        return escalationLevel;
    }

    public void setEscalationLevel(Integer escalationLevel) {
        this.escalationLevel = escalationLevel;
    }

    public FeedbackDto getFeedback() {
        return feedback;
    }

    public void setFeedback(FeedbackDto feedback) {
        this.feedback = feedback;
    }

    public Boolean getHasFeedback() {
        return hasFeedback;
    }

    public void setHasFeedback(Boolean hasFeedback) {
        this.hasFeedback = hasFeedback;
    }
}
