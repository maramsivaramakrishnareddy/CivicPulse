package com.smartgovernance.controller;

import com.smartgovernance.dto.*;
import com.smartgovernance.service.OfficerService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/officer")
public class OfficerController {
    private final OfficerService officerService;
    public OfficerController(OfficerService officerService) { this.officerService = officerService; }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse> getDashboard(HttpSession session) {
        return ResponseEntity.ok(ApiResponse.success("Officer dashboard retrieved", officerService.getDashboard(requireOfficerId(session))));
    }

    @GetMapping("/my-complaints")
    public ResponseEntity<ApiResponse> getMyComplaints(HttpSession session) {
        return ResponseEntity.ok(ApiResponse.success("Assigned complaints retrieved", officerService.getMyComplaints(requireOfficerId(session))));
    }

    @GetMapping("/my-complaints/{complaintId}")
    public ResponseEntity<ApiResponse> getMyComplaintById(@PathVariable String complaintId, HttpSession session) {
        return ResponseEntity.ok(ApiResponse.success("Complaint detail retrieved", officerService.getMyComplaintById(requireOfficerId(session), complaintId)));
    }

    @PutMapping("/my-complaints/{complaintId}/status")
    public ResponseEntity<ApiResponse> updateMyComplaintStatus(@PathVariable String complaintId, @Valid @RequestBody OfficerComplaintUpdateRequest request, HttpSession session) {
        return ResponseEntity.ok(ApiResponse.success("Complaint status updated", officerService.updateMyComplaintStatus(requireOfficerId(session), complaintId, request)));
    }

    @GetMapping("/my-complaints/{complaintId}/history")
    public ResponseEntity<ApiResponse> getMyComplaintHistory(@PathVariable String complaintId, HttpSession session) {
        return ResponseEntity.ok(ApiResponse.success("Complaint history retrieved", officerService.getMyComplaintHistory(requireOfficerId(session), complaintId)));
    }

    @GetMapping("/duplicates")
    public ResponseEntity<ApiResponse> getDuplicates(HttpSession session) {
        return ResponseEntity.ok(ApiResponse.success("Possible duplicate complaints retrieved", officerService.getDuplicateComplaints(requireOfficerId(session))));
    }

    @GetMapping("/duplicates/{complaintId}")
    public ResponseEntity<ApiResponse> getDuplicateReview(@PathVariable String complaintId, HttpSession session) {
        return ResponseEntity.ok(ApiResponse.success("Duplicate review retrieved", officerService.getDuplicateReview(requireOfficerId(session), complaintId)));
    }

    @PutMapping("/duplicates/{complaintId}/verify")
    public ResponseEntity<ApiResponse> verifyDuplicate(@PathVariable String complaintId, @Valid @RequestBody ComplaintDuplicateStatusRequest request, HttpSession session) {
        return ResponseEntity.ok(ApiResponse.success("Duplicate verification saved", officerService.verifyDuplicate(requireOfficerId(session), complaintId, request)));
    }

    @GetMapping("/escalated")
    public ResponseEntity<ApiResponse> getEscalated(HttpSession session) {
        return ResponseEntity.ok(ApiResponse.success("Escalated complaints retrieved", officerService.getEscalatedComplaints(requireOfficerId(session))));
    }

    @PostMapping("/my-complaints/{complaintId}/escalate")
    public ResponseEntity<ApiResponse> escalate(@PathVariable String complaintId, @Valid @RequestBody EscalationRequest request, HttpSession session) {
        return ResponseEntity.ok(ApiResponse.success("Complaint escalated", officerService.escalateComplaint(requireOfficerId(session), complaintId, request)));
    }

    @PutMapping("/escalations/{escalationId}/status")
    public ResponseEntity<ApiResponse> updateEscalationStatus(
            @PathVariable Long escalationId,
            @Valid @RequestBody EscalationStatusUpdateRequest request,
            HttpSession session) {
        String status = request.getStatus();
        String remarks = request.getRemarks();
        return ResponseEntity.ok(ApiResponse.success("Escalation status updated",
                officerService.updateEscalationStatus(requireOfficerId(session), escalationId, status, remarks)));
    }

    private Long requireOfficerId(HttpSession session) {
        String userType = (String) session.getAttribute("userType");
        if (!"OFFICER".equalsIgnoreCase(userType)) throw new IllegalArgumentException("Only officers can access this resource.");
        Long id = (Long) session.getAttribute("userId");
        if (id == null) throw new IllegalArgumentException("Officer session is missing.");
        return id;
    }
}
