package com.smartgovernance.controller;

import com.smartgovernance.dto.*;
import com.smartgovernance.entity.ComplaintHistory;
import com.smartgovernance.entity.Department;
import com.smartgovernance.repository.DepartmentRepository;
import com.smartgovernance.service.ComplaintService;
import com.smartgovernance.service.ai.GeminiService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ComplaintController {

    private final ComplaintService complaintService;
    private final DepartmentRepository departmentRepository;
    private final GeminiService geminiService;

    public ComplaintController(ComplaintService complaintService,
                                DepartmentRepository departmentRepository,
                                GeminiService geminiService) {
        this.complaintService = complaintService;
        this.departmentRepository = departmentRepository;
        this.geminiService = geminiService;
    }


    @PostMapping("/citizen/complaints/suggest-category")
    public ResponseEntity<ApiResponse> suggestCategory(@RequestBody Map<String, String> body, HttpSession session) {
        getLoggedInCitizenId(session); // citizens only
        String description = body.getOrDefault("description", "");
        List<String> departmentNames = departmentRepository.findAll().stream()
                .map(Department::getName).toList();
        GeminiService.Classification suggestion = geminiService.classifyComplaint(description, departmentNames);
        return ResponseEntity.ok(ApiResponse.success("AI suggestion generated",
                Map.of("departmentName", suggestion.department, "priority", suggestion.priority)));
    }

    @PostMapping(value = "/citizen/complaints", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> createComplaint(@Valid @RequestBody ComplaintCreateRequest request, HttpSession session) {
        return createComplaintInternal(request, session);
    }

    @PostMapping(value = "/citizen/complaints", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> createComplaintMultipart(
            @Valid @RequestPart("complaint") ComplaintCreateRequest request,
            @RequestPart(value = "video", required = false) MultipartFile video,
            HttpSession session) {
        if (video != null && !video.isEmpty()) {
            request.setVideoUrl(convertVideoToDataUrl(video));
        }
        return createComplaintInternal(request, session);
    }

    private ResponseEntity<ApiResponse> createComplaintInternal(ComplaintCreateRequest request, HttpSession session) {
        Long citizenId = getLoggedInCitizenId(session);
        ComplaintResponseDto response = complaintService.createComplaint(citizenId, request);
        return ResponseEntity.ok(ApiResponse.success("Complaint submitted successfully", response));
    }

    private String convertVideoToDataUrl(MultipartFile file) {
        try {
            String base64 = Base64.getEncoder().encodeToString(file.getBytes());
            String contentType = file.getContentType() != null ? file.getContentType() : "video/webm";
            return "data:" + contentType + ";base64," + base64;
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to process recorded video.", e);
        }
    }

    @GetMapping("/citizen/complaints")
    public ResponseEntity<ApiResponse> getCitizenComplaints(HttpSession session) {
        Long citizenId = getLoggedInCitizenId(session);
        List<ComplaintResponseDto> complaints = complaintService.getCitizenComplaints(citizenId);
        return ResponseEntity.ok(ApiResponse.success("Citizen complaints retrieved", complaints));
    }

    @GetMapping("/officer/complaints")
    public ResponseEntity<ApiResponse> getAllComplaints(HttpSession session) {
        String userType = (String) session.getAttribute("userType");
        if (!"OFFICER".equalsIgnoreCase(userType)) {
            throw new IllegalArgumentException("Only officers can access the complaint queue.");
        }
        Long officerId = (Long) session.getAttribute("userId");
        if (officerId == null) {
            throw new IllegalArgumentException("Officer session is missing.");
        }
        List<ComplaintResponseDto> complaints = complaintService.getOfficerComplaints(officerId);
        return ResponseEntity.ok(ApiResponse.success("Department complaints retrieved", complaints));
    }

    @GetMapping("/complaints/{complaintId}")
    public ResponseEntity<ApiResponse> getComplaint(@PathVariable String complaintId, HttpSession session) {
        String userType = (String) session.getAttribute("userType");
        if (userType == null) {
            throw new IllegalArgumentException("You must be logged in to view complaint details.");
        }
        ComplaintResponseDto complaint = complaintService.getComplaintById(complaintId);
        return ResponseEntity.ok(ApiResponse.success("Complaint retrieved", complaint));
    }

    @PutMapping("/officer/complaints/{complaintId}/status")
    public ResponseEntity<ApiResponse> updateComplaintStatus(
            @PathVariable String complaintId,
            @Valid @RequestBody ComplaintStatusUpdateRequest request,
            HttpSession session) {
        String userType = (String) session.getAttribute("userType");
        if (!"OFFICER".equalsIgnoreCase(userType)) {
            throw new IllegalArgumentException("Only officers can update complaint status.");
        }
        Long officerId = (Long) session.getAttribute("userId");
        ComplaintResponseDto response = complaintService.updateComplaintStatus(complaintId, request, officerId);
        return ResponseEntity.ok(ApiResponse.success("Complaint status updated", response));
    }

    @PostMapping({"/complaints/{complaintId}/feedback", "/citizen/complaints/{complaintId}/feedback"})
    public ResponseEntity<ApiResponse> submitFeedback(
            @PathVariable String complaintId,
            @Valid @RequestBody FeedbackRequest request,
            HttpSession session) {
        Long citizenId = getLoggedInCitizenId(session);
        ComplaintResponseDto response = complaintService.submitFeedback(complaintId, citizenId, request);
        return ResponseEntity.ok(ApiResponse.success("Feedback submitted successfully", response));
    }

    @PostMapping({"/complaints/{complaintId}/reopen", "/citizen/complaints/{complaintId}/reopen"})
    public ResponseEntity<ApiResponse> requestReopen(
            @PathVariable String complaintId,
            @Valid @RequestBody ReopenRequestDto request,
            HttpSession session) {
        Long citizenId = getLoggedInCitizenId(session);
        ComplaintResponseDto response = complaintService.requestReopen(complaintId, citizenId, request);
        return ResponseEntity.ok(ApiResponse.success("Reopen request submitted", response));
    }

    @PutMapping("/officer/complaints/{complaintId}/duplicate-status")
    public ResponseEntity<ApiResponse> setDuplicateStatus(
            @PathVariable String complaintId,
            @Valid @RequestBody ComplaintDuplicateStatusRequest request,
            HttpSession session) {
        String userType = (String) session.getAttribute("userType");
        if (!"OFFICER".equalsIgnoreCase(userType)) {
            throw new IllegalArgumentException("Only officers can set duplicate status.");
        }
        Long officerId = (Long) session.getAttribute("userId");
        ComplaintResponseDto response = complaintService.setDuplicateStatus(complaintId, request, officerId);
        return ResponseEntity.ok(ApiResponse.success("Duplicate status updated", response));
    }

    @PostMapping("/officer/complaints/{complaintId}/escalate")
    public ResponseEntity<ApiResponse> escalateComplaint(
            @PathVariable String complaintId,
            @Valid @RequestBody EscalationRequest request,
            HttpSession session) {
        String userType = (String) session.getAttribute("userType");
        if (!"OFFICER".equalsIgnoreCase(userType)) {
            throw new IllegalArgumentException("Only officers can escalate complaints.");
        }
        Long officerId = (Long) session.getAttribute("userId");
        ComplaintResponseDto response = complaintService.escalateComplaint(complaintId, officerId, request);
        return ResponseEntity.ok(ApiResponse.success("Complaint escalated", response));
    }

    @PostMapping("/citizen/complaints/{complaintId}/escalate")
    public ResponseEntity<ApiResponse> escalateComplaintByCitizen(
            @PathVariable String complaintId,
            @Valid @RequestBody EscalationRequest request,
            HttpSession session) {
        Long citizenId = getLoggedInCitizenId(session);
        ComplaintResponseDto response = complaintService.escalateComplaintByCitizen(complaintId, citizenId, request);
        return ResponseEntity.ok(ApiResponse.success("Complaint escalated successfully", response));
    }

    @PostMapping("/complaints/{complaintId}/escalate")
    public ResponseEntity<ApiResponse> universalEscalate(
            @PathVariable String complaintId,
            @Valid @RequestBody EscalationRequest request,
            HttpSession session) {
        String userType = (String) session.getAttribute("userType");
        if ("OFFICER".equalsIgnoreCase(userType)) {
            Long officerId = (Long) session.getAttribute("userId");
            ComplaintResponseDto response = complaintService.escalateComplaint(complaintId, officerId, request);
            return ResponseEntity.ok(ApiResponse.success("Complaint escalated", response));
        } else if ("CITIZEN".equalsIgnoreCase(userType)) {
            Long citizenId = (Long) session.getAttribute("userId");
            ComplaintResponseDto response = complaintService.escalateComplaintByCitizen(complaintId, citizenId, request);
            return ResponseEntity.ok(ApiResponse.success("Complaint escalated successfully", response));
        }
        throw new IllegalArgumentException("Authentication required to escalate complaint.");
    }

    @GetMapping("/officer/complaints/emergency")
    public ResponseEntity<ApiResponse> getEmergencyComplaints(HttpSession session) {
        String userType = (String) session.getAttribute("userType");
        if (!"OFFICER".equalsIgnoreCase(userType)) {
            throw new IllegalArgumentException("Only officers can access this resource.");
        }
        List<ComplaintResponseDto> complaints = complaintService.getEmergencyComplaints();
        return ResponseEntity.ok(ApiResponse.success("Emergency complaints retrieved", complaints));
    }

    @GetMapping("/officer/complaints/reopened")
    public ResponseEntity<ApiResponse> getReopenedComplaints(HttpSession session) {
        String userType = (String) session.getAttribute("userType");
        if (!"OFFICER".equalsIgnoreCase(userType)) {
            throw new IllegalArgumentException("Only officers can access this resource.");
        }
        List<ComplaintResponseDto> complaints = complaintService.getReopenedComplaints();
        return ResponseEntity.ok(ApiResponse.success("Reopened complaints retrieved", complaints));
    }

    @GetMapping("/complaints/{complaintId}/history")
    public ResponseEntity<ApiResponse> getComplaintHistory(@PathVariable String complaintId, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            throw new IllegalArgumentException("You must be logged in to view complaint history.");
        }
        List<ComplaintHistory> history = complaintService.getComplaintHistory(complaintId);
        return ResponseEntity.ok(ApiResponse.success("Complaint history retrieved", history));
    }

    private Long getLoggedInCitizenId(HttpSession session) {
        String userType = (String) session.getAttribute("userType");
        if (!"CITIZEN".equalsIgnoreCase(userType)) {
            throw new IllegalArgumentException("Only citizens can perform this action.");
        }
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            throw new IllegalArgumentException("Citizen session is missing.");
        }
        return userId;
    }
}
