package com.smartgovernance.service;

import com.smartgovernance.dto.*;
import java.util.List;

public interface OfficerService {
    OfficerDashboardDto getDashboard(Long officerId);
    List<OfficerComplaintSummaryDto> getMyComplaints(Long officerId);
    OfficerComplaintDetailDto getMyComplaintById(Long officerId, String complaintId);
    OfficerComplaintDetailDto updateMyComplaintStatus(Long officerId, String complaintId, OfficerComplaintUpdateRequest request);
    List<ComplaintHistoryEntryDto> getMyComplaintHistory(Long officerId, String complaintId);
    List<OfficerDuplicateSummaryDto> getDuplicateComplaints(Long officerId);
    OfficerDuplicateReviewDto getDuplicateReview(Long officerId, String complaintId);
    OfficerComplaintDetailDto verifyDuplicate(Long officerId, String complaintId, ComplaintDuplicateStatusRequest request);
    List<OfficerEscalationSummaryDto> getEscalatedComplaints(Long officerId);
    OfficerComplaintDetailDto escalateComplaint(Long officerId, String complaintId, EscalationRequest request);
    OfficerComplaintDetailDto updateEscalationStatus(Long officerId, Long escalationId, String status, String remarks);
}
