package com.smartgovernance.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaint", indexes = {
    @Index(name = "idx_complaint_citizen", columnList = "citizen_id"),
    @Index(name = "idx_complaint_department", columnList = "department_id"),
    @Index(name = "idx_complaint_officer", columnList = "officer_id"),
    @Index(name = "idx_complaint_status", columnList = "status")
})
public class Complaint {

    @Id
    @Column(name = "complaint_id", nullable = false, length = 50)
    private String complaintId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_id", nullable = false)
    private Citizen citizen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "officer_id")
    private Officer officer;

    @Enumerated(EnumType.STRING)
    @Column(name = "complaint_type", nullable = false, length = 20)
    private ComplaintType complaintType = ComplaintType.NORMAL;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", columnDefinition = "LONGTEXT")
    private String imageUrl;

    @Column(name = "video_url", columnDefinition = "LONGTEXT")
    private String videoUrl;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "location", nullable = false, length = 255)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ComplaintStatus status = ComplaintStatus.APPLIED;

    @Enumerated(EnumType.STRING)
    @Column(name = "duplicate_status", nullable = false, length = 30)
    private DuplicateStatus duplicateStatus = DuplicateStatus.PENDING_REVIEW;

    @Column(name = "action_taken", columnDefinition = "TEXT")
    private String actionTaken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "duplicate_of_complaint_id")
    private Complaint duplicateOf;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "ai_priority", length = 20)
    private String aiPriority;

    @Column(name = "ai_photo_mismatch_suspected")
    private Boolean aiPhotoMismatchSuspected = false;

    @Column(name = "ai_photo_mismatch_reason", length = 500)
    private String aiPhotoMismatchReason;

    public Complaint() {
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = ComplaintStatus.APPLIED;
        }
        if (this.duplicateStatus == null) {
            this.duplicateStatus = DuplicateStatus.PENDING_REVIEW;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum ComplaintType {
        NORMAL,
        EMERGENCY
    }

    public enum ComplaintStatus {
        APPLIED,
        COMPLAINT_VIEWED,
        ACTION_TO_BE_TAKEN,
        COMPLAINT_RESOLVED,
        REOPENED,
        DUPLICATE,
        REJECTED
    }

    public enum DuplicateStatus {
        PENDING_REVIEW,
        DUPLICATE,
        NOT_DUPLICATE
    }

    public String getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(String complaintId) {
        this.complaintId = complaintId;
    }

    public Citizen getCitizen() {
        return citizen;
    }

    public void setCitizen(Citizen citizen) {
        this.citizen = citizen;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Officer getOfficer() {
        return officer;
    }

    public void setOfficer(Officer officer) {
        this.officer = officer;
    }

    public ComplaintType getComplaintType() {
        return complaintType;
    }

    public void setComplaintType(ComplaintType complaintType) {
        this.complaintType = complaintType;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }

    public DuplicateStatus getDuplicateStatus() {
        return duplicateStatus;
    }

    public void setDuplicateStatus(DuplicateStatus duplicateStatus) {
        this.duplicateStatus = duplicateStatus;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
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

    public Complaint getDuplicateOf() {
        return duplicateOf;
    }

    public void setDuplicateOf(Complaint duplicateOf) {
        this.duplicateOf = duplicateOf;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
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
}
