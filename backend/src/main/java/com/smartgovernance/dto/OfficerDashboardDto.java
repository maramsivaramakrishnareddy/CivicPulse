package com.smartgovernance.dto;

import java.util.List;

/**
 * Officer Module DTO: full payload for the Officer Dashboard page.
 * Used by GET /api/officer/dashboard.
 */
public class OfficerDashboardDto {

    private String officerName;
    private String department;
    private String employeeId;
    private long resolvedCount;
    private long pendingCount;
    private long rejectedCount;
    private long inProgressCount;
    private long totalCount;
    private long reopenedCount;
    private long duplicateCount;
    private long escalatedCount;
    private long attentionCount;
    private long pendingDuplicateCount;
    private long emergencyPendingCount;
    private List<OfficerComplaintSummaryDto> recentComplaints;

    public String getOfficerName() {
        return officerName;
    }

    public void setOfficerName(String officerName) {
        this.officerName = officerName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public long getResolvedCount() {
        return resolvedCount;
    }

    public void setResolvedCount(long resolvedCount) {
        this.resolvedCount = resolvedCount;
    }

    public long getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(long pendingCount) {
        this.pendingCount = pendingCount;
    }

    public long getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(long rejectedCount) {
        this.rejectedCount = rejectedCount;
    }

    public long getInProgressCount() {
        return inProgressCount;
    }

    public void setInProgressCount(long inProgressCount) {
        this.inProgressCount = inProgressCount;
    }

    public long getTotalCount() { return totalCount; }
    public void setTotalCount(long totalCount) { this.totalCount = totalCount; }
    public long getReopenedCount() { return reopenedCount; }
    public void setReopenedCount(long reopenedCount) { this.reopenedCount = reopenedCount; }
    public long getDuplicateCount() { return duplicateCount; }
    public void setDuplicateCount(long duplicateCount) { this.duplicateCount = duplicateCount; }
    public long getEscalatedCount() { return escalatedCount; }
    public void setEscalatedCount(long escalatedCount) { this.escalatedCount = escalatedCount; }
    public long getAttentionCount() { return attentionCount; }
    public void setAttentionCount(long attentionCount) { this.attentionCount = attentionCount; }
    public long getPendingDuplicateCount() { return pendingDuplicateCount; }
    public void setPendingDuplicateCount(long pendingDuplicateCount) { this.pendingDuplicateCount = pendingDuplicateCount; }
    public long getEmergencyPendingCount() { return emergencyPendingCount; }

    public void setEmergencyPendingCount(long emergencyPendingCount) { this.emergencyPendingCount = emergencyPendingCount; }

    public List<OfficerComplaintSummaryDto> getRecentComplaints() {
        return recentComplaints;
    }

    public void setRecentComplaints(List<OfficerComplaintSummaryDto> recentComplaints) {
        this.recentComplaints = recentComplaints;
    }
}
