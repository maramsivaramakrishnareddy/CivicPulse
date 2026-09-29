package com.smartgovernance.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "escalation", indexes = {
    @Index(name = "idx_escalation_complaint", columnList = "complaint_id")
})
public class Escalation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "escalation_id")
    private Long escalationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escalated_by_officer_id", nullable = false)
    private Officer escalatedByOfficer;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "escalation_level", nullable = false)
    private Integer escalationLevel = 1;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "target", length = 150)
    private String target;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "PENDING";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Escalation() {
    }

    public Escalation(Complaint complaint, Officer escalatedByOfficer, String reason, Integer escalationLevel) {
        this.complaint = complaint;
        this.escalatedByOfficer = escalatedByOfficer;
        this.reason = reason;
        this.escalationLevel = escalationLevel;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Long getEscalationId() {
        return escalationId;
    }

    public void setEscalationId(Long escalationId) {
        this.escalationId = escalationId;
    }

    public Complaint getComplaint() {
        return complaint;
    }

    public void setComplaint(Complaint complaint) {
        this.complaint = complaint;
    }

    public Officer getEscalatedByOfficer() {
        return escalatedByOfficer;
    }

    public void setEscalatedByOfficer(Officer escalatedByOfficer) {
        this.escalatedByOfficer = escalatedByOfficer;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Integer getEscalationLevel() {
        return escalationLevel;
    }

    public void setEscalationLevel(Integer escalationLevel) {
        this.escalationLevel = escalationLevel;
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
}
