package com.smartgovernance.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaint_history", indexes = {
    @Index(name = "idx_history_complaint", columnList = "complaint_id")
})
public class ComplaintHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    @JsonIgnore
    private Complaint complaint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "officer_id", nullable = true)
    @JsonIgnore
    private Officer officer;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private Complaint.ComplaintStatus status;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ComplaintHistory() {
    }

    public ComplaintHistory(Complaint complaint, Complaint.ComplaintStatus status, String message) {
        this.complaint = complaint;
        this.status = status;
        this.message = message;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Long getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }

    public Complaint getComplaint() {
        return complaint;
    }

    public void setComplaint(Complaint complaint) {
        this.complaint = complaint;
    }

    @JsonIgnore
    public Officer getOfficer() {
        return officer;
    }

    @JsonProperty("officerName")
    public String getOfficerName() {
        try {
            return (officer != null) ? officer.getFullName() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public void setOfficer(Officer officer) {
        this.officer = officer;
    }

    public Complaint.ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(Complaint.ComplaintStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
