package com.smartgovernance.service;

import com.smartgovernance.dto.*;
import com.smartgovernance.entity.Complaint;
import com.smartgovernance.entity.ComplaintHistory;

import java.util.List;

public interface ComplaintService {
    ComplaintResponseDto createComplaint(Long citizenId, ComplaintCreateRequest request);
    List<ComplaintResponseDto> getCitizenComplaints(Long citizenId);
    List<ComplaintResponseDto> getAllComplaints();
    List<ComplaintResponseDto> getOfficerComplaints(Long officerId);
    List<ComplaintResponseDto> getDepartmentComplaints(Long departmentId);
    ComplaintResponseDto getComplaintById(String complaintId);
    ComplaintResponseDto updateComplaintStatus(String complaintId, ComplaintStatusUpdateRequest request, Long officerId);
    ComplaintResponseDto setDuplicateStatus(String complaintId, ComplaintDuplicateStatusRequest request, Long officerId);
    ComplaintResponseDto submitFeedback(String complaintId, Long citizenId, FeedbackRequest request);
    ComplaintResponseDto requestReopen(String complaintId, Long citizenId, ReopenRequestDto request);
    ComplaintResponseDto escalateComplaint(String complaintId, Long officerId, EscalationRequest request);
    ComplaintResponseDto escalateComplaintByCitizen(String complaintId, Long citizenId, EscalationRequest request);
    List<ComplaintHistory> getComplaintHistory(String complaintId);
    List<ComplaintResponseDto> getEmergencyComplaints();
    List<ComplaintResponseDto> getReopenedComplaints();
}
