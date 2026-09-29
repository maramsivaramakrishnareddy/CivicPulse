package com.smartgovernance.service.impl;

import com.smartgovernance.dto.*;
import com.smartgovernance.entity.*;
import com.smartgovernance.repository.*;
import com.smartgovernance.service.ComplaintService;
import com.smartgovernance.service.NotificationService;
import com.smartgovernance.service.ai.GeminiService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComplaintServiceImpl implements ComplaintService {

    private static final double DUPLICATE_TEXT_THRESHOLD = 0.70;
    private static final double DUPLICATE_RADIUS_METRES = 500.0;

    private final ComplaintRepository complaintRepository;
    private final CitizenRepository citizenRepository;
    private final DepartmentRepository departmentRepository;
    private final OfficerRepository officerRepository;
    private final ComplaintHistoryRepository complaintHistoryRepository;
    private final FeedbackRepository feedbackRepository;
    private final ReopenRequestRepository reopenRequestRepository;
    private final EscalationRepository escalationRepository;
    private final GeminiService geminiService;
    private final NotificationService notificationService;

    public ComplaintServiceImpl(ComplaintRepository complaintRepository,
                               CitizenRepository citizenRepository,
                               DepartmentRepository departmentRepository,
                               OfficerRepository officerRepository,
                               ComplaintHistoryRepository complaintHistoryRepository,
                               FeedbackRepository feedbackRepository,
                               ReopenRequestRepository reopenRequestRepository,
                               EscalationRepository escalationRepository,
                               GeminiService geminiService,
                               NotificationService notificationService) {
        this.complaintRepository = complaintRepository;
        this.citizenRepository = citizenRepository;
        this.departmentRepository = departmentRepository;
        this.officerRepository = officerRepository;
        this.complaintHistoryRepository = complaintHistoryRepository;
        this.feedbackRepository = feedbackRepository;
        this.reopenRequestRepository = reopenRequestRepository;
        this.escalationRepository = escalationRepository;
        this.geminiService = geminiService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public ComplaintResponseDto createComplaint(Long citizenId, ComplaintCreateRequest request) {
        Citizen citizen = citizenRepository.findById(citizenId)
                .orElseThrow(() -> new IllegalArgumentException("Citizen not found."));

        // Defense-in-depth: mandatory-photo is already enforced via @NotBlank + @Valid on the
        // controller, but multipart @RequestPart validation has known quirks across Spring
        // versions, so this backstop guarantees it's never silently skipped either way.
        if (request.getImageUrl() == null || request.getImageUrl().isBlank()) {
            throw new IllegalArgumentException("A live camera photo is required to file a complaint.");
        }

        String requestedDepartment = request.getDepartmentName() == null ? "" : request.getDepartmentName().trim();
        if (requestedDepartment.isBlank()) {
            throw new IllegalArgumentException("Department is required.");
        }
        Department department = departmentRepository.findAll().stream()
                .filter(d -> d.getName() != null && d.getName().trim().equalsIgnoreCase(requestedDepartment))
                .findFirst()
                .orElseGet(() -> departmentRepository.save(new Department(requestedDepartment, "Auto-created department")));

        Complaint complaint = new Complaint();
        complaint.setComplaintId(generateComplaintId());
        complaint.setCitizen(citizen);
        complaint.setDepartment(department);
        complaint.setTitle(request.getTitle().trim());
        complaint.setDescription(request.getDescription().trim());
        complaint.setLocation(request.getLocation().trim());
        complaint.setImageUrl(request.getImageUrl());
        complaint.setVideoUrl(request.getVideoUrl());
        complaint.setLatitude(request.getLatitude());
        complaint.setLongitude(request.getLongitude());
        complaint.setComplaintType(parseComplaintType(request.getComplaintType()));

        // AI-assisted layer (advisory only - does not change department/complaintType/status logic above):
        //  - aiPriority: Gemini's independent read of urgency from the description text
        //  - aiPhotoMismatchSuspected/Reason: flags when the uploaded photo doesn't plausibly
        //    match the written description, for the officer to see during review
        complaint.setAiPriority(geminiService.detectPriority(complaint.getDescription()));
        if (complaint.getImageUrl() != null && !complaint.getImageUrl().isBlank()) {
            GeminiService.PhotoMatchResult photoMatch =
                    geminiService.verifyPhotoMatchesDescription(complaint.getImageUrl(), complaint.getDescription());
            if (!photoMatch.matches) {
                complaint.setAiPhotoMismatchSuspected(true);
                complaint.setAiPhotoMismatchReason(photoMatch.reason);
            }
        }

        // Route the citizen complaint to the first provisioned officer for the selected department.
        // This preserves the single Complaint record while making the existing department assignment explicit.
        officerRepository.findAll().stream()
                .filter(o -> o.getDepartment() != null
                        && o.getDepartment().trim().equalsIgnoreCase(department.getName().trim()))
                .findFirst()
                .ifPresent(complaint::setOfficer);

        complaint.setStatus(Complaint.ComplaintStatus.APPLIED);
        complaint.setDuplicateStatus(Complaint.DuplicateStatus.PENDING_REVIEW);

        Complaint saved = complaintRepository.save(complaint);
        complaintHistoryRepository.save(new ComplaintHistory(saved, saved.getStatus(), "Complaint was submitted by the citizen."));

        try {
            notificationService.sendNotification(
                    citizen,
                    "Complaint Registered: " + saved.getTitle(),
                    "Your complaint (" + saved.getComplaintId() + ") has been successfully submitted and assigned to "
                            + (saved.getDepartment() != null ? saved.getDepartment().getName() : "the respective department") + ".",
                    "COMPLAINT_SUBMITTED",
                    saved
            );
        } catch (Exception e) {
            // Notification failure should not fail complaint registration
        }

        return toResponse(saved);
    }

    @Override
    @Transactional
    public List<ComplaintResponseDto> getCitizenComplaints(Long citizenId) {
        Citizen citizen = citizenRepository.findById(citizenId)
                .orElseThrow(() -> new IllegalArgumentException("Citizen not found."));
        return complaintRepository.findByCitizenOrderByCreatedAtDesc(citizen)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<ComplaintResponseDto> getAllComplaints() {
        return complaintRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

        @Override
        @Transactional(readOnly = true)
        public List<ComplaintResponseDto> getOfficerComplaints(Long officerId) {
        Officer officer = officerRepository.findById(officerId)
            .orElseThrow(() -> new IllegalArgumentException("Officer not found."));
        // Keep this endpoint synchronized with the Officer module: every complaint
        // belonging to the officer's department is visible, including newly created
        // complaints that have not yet been explicitly claimed by an officer.
        return complaintRepository.findByDepartmentNameOrderByCreatedAtDesc(officer.getDepartment())
            .stream().map(this::toResponse).collect(Collectors.toList());
        }

    @Override
    @Transactional
    public ComplaintResponseDto getComplaintById(String complaintId) {
        Complaint complaint = complaintRepository.findComplaintWithDetails(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found."));
        return toResponse(complaint);
    }

    @Override
    @Transactional
    public ComplaintResponseDto updateComplaintStatus(String complaintId, ComplaintStatusUpdateRequest request, Long officerId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found."));

        Officer officer = officerRepository.findById(officerId)
                .orElseThrow(() -> new IllegalArgumentException("Officer not found."));

        if (complaint.getDepartment() == null
                || !complaint.getDepartment().getName().equalsIgnoreCase(officer.getDepartment())) {
            throw new IllegalArgumentException("Officer can only manage complaints assigned to their department.");
        }

        Complaint.ComplaintStatus newStatus = parseStatus(request.getStatus());
        if (!isValidStatusTransition(complaint.getStatus(), newStatus)) {
            throw new IllegalArgumentException("Invalid status transition from " + complaint.getStatus() + " to " + newStatus + ".");
        }
        complaint.setStatus(newStatus);
        complaint.setOfficer(officer);
        complaint.setActionTaken(request.getActionTaken());
        if (newStatus == Complaint.ComplaintStatus.COMPLAINT_RESOLVED) {
            complaint.setResolvedAt(LocalDateTime.now());
        }

        complaintRepository.save(complaint);
        complaintHistoryRepository.save(new ComplaintHistory(complaint, newStatus,
                request.getActionTaken() == null || request.getActionTaken().isBlank()
                        ? "Complaint status updated by officer."
                        : "Complaint status updated by officer: " + request.getActionTaken()));

        if (complaint.getCitizen() != null) {
            try {
                String type = (newStatus == Complaint.ComplaintStatus.COMPLAINT_RESOLVED) ? "COMPLAINT_RESOLVED" : "STATUS_CHANGED";
                String desc = (newStatus == Complaint.ComplaintStatus.COMPLAINT_RESOLVED)
                        ? "Your complaint (" + complaint.getComplaintId() + ") has been marked as resolved by Officer " + officer.getFullName() + "."
                        : "Your complaint (" + complaint.getComplaintId() + ") status has been updated to " + newStatus + ".";
                if (request.getActionTaken() != null && !request.getActionTaken().isBlank()) {
                    desc += " Remarks: " + request.getActionTaken();
                }
                notificationService.sendNotification(
                        complaint.getCitizen(),
                        "Complaint Status: " + newStatus,
                        desc,
                        type,
                        complaint
                );
            } catch (Exception e) {
                // Notification failure should not fail status update
            }
        }

        return toResponse(complaint);
    }

    private boolean isValidStatusTransition(Complaint.ComplaintStatus currentStatus,
                                             Complaint.ComplaintStatus newStatus) {
        if (currentStatus == Complaint.ComplaintStatus.DUPLICATE) {
            return newStatus == Complaint.ComplaintStatus.DUPLICATE;
        }
        if (newStatus == Complaint.ComplaintStatus.REJECTED) {
            return currentStatus != Complaint.ComplaintStatus.COMPLAINT_RESOLVED;
        }
        // An officer can resolve a complaint at any operational stage (APPLIED, COMPLAINT_VIEWED, ACTION_TO_BE_TAKEN, REOPENED)
        if (newStatus == Complaint.ComplaintStatus.COMPLAINT_RESOLVED) {
            return true;
        }
        return (currentStatus == Complaint.ComplaintStatus.APPLIED
                && (newStatus == Complaint.ComplaintStatus.COMPLAINT_VIEWED || newStatus == Complaint.ComplaintStatus.ACTION_TO_BE_TAKEN))
                || (currentStatus == Complaint.ComplaintStatus.COMPLAINT_VIEWED
                && newStatus == Complaint.ComplaintStatus.ACTION_TO_BE_TAKEN)
                || (currentStatus == Complaint.ComplaintStatus.REOPENED
                && (newStatus == Complaint.ComplaintStatus.COMPLAINT_VIEWED || newStatus == Complaint.ComplaintStatus.ACTION_TO_BE_TAKEN))
                || (currentStatus == newStatus);
    }

    @Override
    @Transactional
    public ComplaintResponseDto submitFeedback(String complaintId, Long citizenId, FeedbackRequest request) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found."));
        Citizen citizen = citizenRepository.findById(citizenId)
                .orElseThrow(() -> new IllegalArgumentException("Citizen not found."));

        if (!complaint.getCitizen().getCitizenId().equals(citizenId)) {
            throw new IllegalArgumentException("Citizen is not authorized to submit feedback for this complaint.");
        }

        if (complaint.getStatus() != Complaint.ComplaintStatus.COMPLAINT_RESOLVED) {
            throw new IllegalArgumentException("Feedback can only be submitted after the complaint is resolved.");
        }

        if (feedbackRepository.existsByComplaint(complaint)) {
            throw new IllegalArgumentException("Feedback has already been submitted for this complaint.");
        }

        Feedback feedback = new Feedback(complaint, citizen, request.getRating(), request.getComment());
        feedback.setImageUrl(request.getImageUrl());
        feedback.setVideoUrl(request.getVideoUrl());
        feedback.setLatitude(request.getLatitude());
        feedback.setLongitude(request.getLongitude());
        feedback.setLocation(request.getLocation());
        feedbackRepository.save(feedback);

        complaintHistoryRepository.save(new ComplaintHistory(complaint, complaint.getStatus(),
                "Citizen submitted feedback with rating: " + request.getRating() + "/5."));

        try {
            notificationService.sendNotification(
                    citizen,
                    "Feedback Received",
                    "Thank you! Your feedback for complaint (" + complaint.getComplaintId() + ") with rating "
                            + request.getRating() + "/5 has been recorded.",
                    "FEEDBACK_RECEIVED",
                    complaint
            );
        } catch (Exception e) {
            // Notification failure should not fail feedback submission
        }

        return toResponse(complaint);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintHistory> getComplaintHistory(String complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found."));
        List<ComplaintHistory> histories = complaintHistoryRepository.findByComplaintOrderByCreatedAtDesc(complaint);
        // Pre-initialize officer proxy to avoid any Jackson serialization lazy issues
        for (ComplaintHistory h : histories) {
            if (h.getOfficer() != null) {
                h.getOfficer().getFullName();
            }
        }
        return histories;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintResponseDto> getDepartmentComplaints(Long departmentId) {
        // Implementation will iterate through all complaints by status and filter by department
        return complaintRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .filter(c -> c.getDepartment() != null && c.getDepartment().getDepartmentId().equals(departmentId))
                .sorted((a, b) -> {
                    // Emergency first
                    if (a.getComplaintType() == Complaint.ComplaintType.EMERGENCY && b.getComplaintType() != Complaint.ComplaintType.EMERGENCY) return -1;
                    if (a.getComplaintType() != Complaint.ComplaintType.EMERGENCY && b.getComplaintType() == Complaint.ComplaintType.EMERGENCY) return 1;
                    // Then by creation date
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintResponseDto> getEmergencyComplaints() {
        return complaintRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .filter(c -> c.getComplaintType() == Complaint.ComplaintType.EMERGENCY)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintResponseDto> getReopenedComplaints() {
        return complaintRepository.findByStatusOrderByCreatedAtDesc(Complaint.ComplaintStatus.REOPENED)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ComplaintResponseDto setDuplicateStatus(String complaintId, ComplaintDuplicateStatusRequest request, Long officerId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found."));

        Officer officer = officerRepository.findById(officerId)
                .orElseThrow(() -> new IllegalArgumentException("Officer not found."));

        if (complaint.getDepartment() == null
                || !complaint.getDepartment().getName().equalsIgnoreCase(officer.getDepartment())) {
            throw new IllegalArgumentException("Officer can only verify complaints assigned to their department.");
        }

        String duplicateStatus = request.getDuplicateStatus().trim().toUpperCase();
        Complaint.DuplicateStatus resolvedStatus;
        switch (duplicateStatus) {
            case "DUPLICATE" -> resolvedStatus = Complaint.DuplicateStatus.DUPLICATE;
            case "NOT_DUPLICATE" -> resolvedStatus = Complaint.DuplicateStatus.NOT_DUPLICATE;
            default -> throw new IllegalArgumentException("Duplicate status must be DUPLICATE or NOT_DUPLICATE.");
        }
        if (complaint.getStatus() == Complaint.ComplaintStatus.DUPLICATE
                && resolvedStatus != Complaint.DuplicateStatus.DUPLICATE) {
            throw new IllegalArgumentException("A terminated duplicate complaint cannot be reopened.");
        }
        complaint.setDuplicateStatus(resolvedStatus);
        if (resolvedStatus == Complaint.DuplicateStatus.DUPLICATE) {
            complaint.setStatus(Complaint.ComplaintStatus.DUPLICATE);
            complaint.setActionTaken("Complaint terminated as a duplicate by officer.");
        }

        complaintRepository.save(complaint);
        String historyMessage = "Duplicate status set to " + duplicateStatus + " by officer.";
        if (request.getNotes() != null && !request.getNotes().isBlank()) {
            historyMessage += " Notes: " + request.getNotes();
        }
        complaintHistoryRepository.save(new ComplaintHistory(complaint, complaint.getStatus(), historyMessage));

        if (complaint.getCitizen() != null && resolvedStatus == Complaint.DuplicateStatus.DUPLICATE) {
            try {
                notificationService.sendNotification(
                        complaint.getCitizen(),
                        "Complaint Marked as Duplicate",
                        "Your complaint (" + complaint.getComplaintId() + ") was reviewed and marked as a duplicate.",
                        "COMPLAINT_MARKED_DUPLICATE",
                        complaint
                );
            } catch (Exception e) {
                // Notification failure should not fail duplicate status update
            }
        }

        return toResponse(complaint);
    }

    @Override
    @Transactional
    public ComplaintResponseDto escalateComplaint(String complaintId, Long officerId, EscalationRequest request) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found."));

        Officer officer = officerRepository.findById(officerId)
                .orElseThrow(() -> new IllegalArgumentException("Officer not found."));

        int level = request.getEscalationLevel() != null ? request.getEscalationLevel() : 1;
        Escalation escalation = new Escalation(complaint, officer, request.getReason(), level);
        escalation.setRemarks(request.getRemarks());
        escalation.setTarget(request.getTarget());
        escalation.setStatus("PENDING");
        escalationRepository.save(escalation);

        complaintHistoryRepository.save(new ComplaintHistory(complaint, complaint.getStatus(),
                "Complaint escalated to level " + level + (request.getTarget() != null ? " (" + request.getTarget() + ")" : "") + ": " + request.getReason()));

        if (complaint.getCitizen() != null) {
            try {
                notificationService.sendNotification(
                        complaint.getCitizen(),
                        "Complaint Escalated",
                        "Your complaint (" + complaint.getComplaintId() + ") has been escalated to Level "
                                + level + (request.getTarget() != null ? " (" + request.getTarget() + ")" : "")
                                + ". Reason: " + request.getReason(),
                        "COMPLAINT_ESCALATED",
                        complaint
                );
            } catch (Exception e) {
                // Notification failure should not fail escalation
            }
        }

        return toResponse(complaint);
    }

    @Override
    @Transactional
    public ComplaintResponseDto escalateComplaintByCitizen(String complaintId, Long citizenId, EscalationRequest request) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found."));
        Citizen citizen = citizenRepository.findById(citizenId)
                .orElseThrow(() -> new IllegalArgumentException("Citizen not found."));

        if (!complaint.getCitizen().getCitizenId().equals(citizenId)) {
            throw new IllegalArgumentException("You are not authorized to escalate this complaint.");
        }

        if (complaint.getStatus() == Complaint.ComplaintStatus.DUPLICATE) {
            throw new IllegalArgumentException("A duplicate complaint cannot be escalated.");
        }

        Officer officer = complaint.getOfficer();
        if (officer == null) {
            officer = officerRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new IllegalStateException("No officer available to route escalation."));
        }

        int level = request.getEscalationLevel() != null && request.getEscalationLevel() >= 1 ? request.getEscalationLevel() : 1;
        String reason = request.getReason() != null ? request.getReason().trim() : "Citizen Escalation Request";
        Escalation escalation = new Escalation(complaint, officer, "[Citizen Escalated] " + reason, level);
        escalation.setRemarks(request.getRemarks());
        escalation.setTarget(request.getTarget() != null && !request.getTarget().isBlank() ? request.getTarget().trim() : "Higher Departmental Authority");
        escalation.setStatus("PENDING");
        escalationRepository.save(escalation);

        complaintHistoryRepository.save(new ComplaintHistory(complaint, complaint.getStatus(),
                "Citizen escalated complaint to Level " + level + " (" + escalation.getTarget() + "): " + reason));

        try {
            notificationService.sendNotification(
                    citizen,
                    "Complaint Escalated",
                    "Your escalation for complaint (" + complaint.getComplaintId() + ") to Level "
                            + level + " has been submitted to " + escalation.getTarget() + ".",
                    "COMPLAINT_ESCALATED",
                    complaint
            );
        } catch (Exception e) {
            // Notification failure should not fail escalation
        }

        return toResponse(complaint);
    }

    @Override
    @Transactional
    public ComplaintResponseDto requestReopen(String complaintId, Long citizenId, ReopenRequestDto request) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found."));
        Citizen citizen = citizenRepository.findById(citizenId)
                .orElseThrow(() -> new IllegalArgumentException("Citizen not found."));

        if (!complaint.getCitizen().getCitizenId().equals(citizenId)) {
            throw new IllegalArgumentException("Citizen is not authorized to reopen this complaint.");
        }

        if (complaint.getStatus() != Complaint.ComplaintStatus.COMPLAINT_RESOLVED) {
            throw new IllegalArgumentException("Only resolved complaints can be reopened.");
        }

        // Require both fresh evidence types before reopening a resolved complaint.
        if (request.getImageUrl() == null || request.getImageUrl().isBlank() ||
            request.getVideoUrl() == null || request.getVideoUrl().isBlank()) {
            throw new IllegalArgumentException("Reopen requires a live evidence image and video.");
        }

        if (request.getLatitude() == null || request.getLongitude() == null || 
            request.getLocation() == null || request.getLocation().isBlank()) {
            throw new IllegalArgumentException("Reopen requires live location (latitude, longitude, and location).");
        }

        ReopenRequest reopenRequest = new ReopenRequest(complaint, citizen, request.getReason());
        reopenRequest.setImageUrl(request.getImageUrl());
        reopenRequest.setVideoUrl(request.getVideoUrl());
        reopenRequest.setLatitude(request.getLatitude());
        reopenRequest.setLongitude(request.getLongitude());
        reopenRequest.setLocation(request.getLocation());
        reopenRequestRepository.save(reopenRequest);

        complaint.setStatus(Complaint.ComplaintStatus.REOPENED);
        complaintRepository.save(complaint);
        complaintHistoryRepository.save(new ComplaintHistory(complaint, complaint.getStatus(), 
                "Reopen request submitted by citizen: " + request.getReason()));

        if (complaint.getCitizen() != null) {
            try {
                notificationService.sendNotification(
                        complaint.getCitizen(),
                        "Complaint Reopened",
                        "Your reopen request for complaint (" + complaint.getComplaintId() + ") has been submitted.",
                        "COMPLAINT_REOPENED",
                        complaint
                );
            } catch (Exception e) {
                // Notification failure should not fail reopen request
            }
        }

        return toResponse(complaint);
    }

    private String generateComplaintId() {
        return "CMP-" + System.currentTimeMillis();
    }

    private Complaint.ComplaintType parseComplaintType(String type) {
        if (type == null) {
            return Complaint.ComplaintType.NORMAL;
        }
        return switch (type.trim().toUpperCase()) {
            case "EMERGENCY" -> Complaint.ComplaintType.EMERGENCY;
            default -> Complaint.ComplaintType.NORMAL;
        };
    }

    private Complaint.ComplaintStatus parseStatus(String status) {
        if (status == null) {
            throw new IllegalArgumentException("Status is required.");
        }
        return switch (status.trim().toUpperCase()) {
            case "COMPLAINT_VIEWED", "VIEWED" -> Complaint.ComplaintStatus.COMPLAINT_VIEWED;
            case "ACTION_TO_BE_TAKEN", "IN_PROGRESS", "PROGRESS", "ACTION" -> Complaint.ComplaintStatus.ACTION_TO_BE_TAKEN;
            case "COMPLAINT_RESOLVED", "RESOLVED" -> Complaint.ComplaintStatus.COMPLAINT_RESOLVED;
            case "REOPENED", "REOPEN" -> Complaint.ComplaintStatus.REOPENED;
            case "APPLIED", "SUBMITTED" -> Complaint.ComplaintStatus.APPLIED;
            case "DUPLICATE" -> Complaint.ComplaintStatus.DUPLICATE;
            case "REJECTED", "REJECT" -> Complaint.ComplaintStatus.REJECTED;
            default -> throw new IllegalArgumentException("Unsupported complaint status: " + status);
        };
    }

    private ComplaintResponseDto toResponse(Complaint complaint) {
        ComplaintResponseDto dto = new ComplaintResponseDto();
        dto.setComplaintId(complaint.getComplaintId());
        dto.setTitle(complaint.getTitle());
        dto.setDescription(complaint.getDescription());
        dto.setDepartmentName(complaint.getDepartment() != null ? complaint.getDepartment().getName() : null);
        dto.setCitizenName(complaint.getCitizen() != null ? complaint.getCitizen().getFullName() : null);
        dto.setOfficerName(complaint.getOfficer() != null ? complaint.getOfficer().getFullName() : null);
        dto.setComplaintType(complaint.getComplaintType() != null ? complaint.getComplaintType().name() : null);
        dto.setStatus(complaint.getStatus() != null ? complaint.getStatus().name() : null);
        dto.setDuplicateStatus(complaint.getDuplicateStatus() != null ? complaint.getDuplicateStatus().name() : null);
        dto.setDuplicateOfComplaintId(complaint.getDuplicateOf() != null ? complaint.getDuplicateOf().getComplaintId() : null);
        DuplicateMatch duplicateMatch = findPossibleDuplicate(complaint);
        if (duplicateMatch != null) {
            dto.setPossibleDuplicateComplaintId(duplicateMatch.complaint().getComplaintId());
            dto.setTextSimilarity(roundPercentage(duplicateMatch.textSimilarity()));
            dto.setDistanceMetres(roundDistance(duplicateMatch.distanceMetres()));
        }
        dto.setLocation(complaint.getLocation());
        dto.setImageUrl(complaint.getImageUrl());
        dto.setVideoUrl(complaint.getVideoUrl());
        dto.setLatitude(complaint.getLatitude());
        dto.setLongitude(complaint.getLongitude());
        dto.setActionTaken(complaint.getActionTaken());
        dto.setAiPriority(complaint.getAiPriority());
        dto.setAiPhotoMismatchSuspected(complaint.getAiPhotoMismatchSuspected());
        dto.setAiPhotoMismatchReason(complaint.getAiPhotoMismatchReason());
        dto.setCreatedAt(complaint.getCreatedAt());
        dto.setUpdatedAt(complaint.getUpdatedAt());
        dto.setResolvedAt(complaint.getResolvedAt());

        List<Escalation> escalations = escalationRepository.findByComplaintOrderByCreatedAtDesc(complaint);
        if (escalations != null && !escalations.isEmpty()) {
            Escalation latest = escalations.get(0);
            dto.setEscalationStatus(latest.getStatus());
            dto.setEscalationReason(latest.getReason());
            dto.setEscalationTarget(latest.getTarget());
            dto.setEscalationLevel(latest.getEscalationLevel());
        }

        feedbackRepository.findByComplaint(complaint).ifPresent(fb -> {
            FeedbackDto fbDto = new FeedbackDto(
                    fb.getFeedbackId(),
                    complaint.getComplaintId(),
                    complaint.getCitizen() != null ? complaint.getCitizen().getFullName() : null,
                    fb.getRating(),
                    fb.getComment(),
                    fb.getCreatedAt()
            );
            fbDto.setImageUrl(fb.getImageUrl());
            fbDto.setVideoUrl(fb.getVideoUrl());
            fbDto.setLatitude(fb.getLatitude());
            fbDto.setLongitude(fb.getLongitude());
            fbDto.setLocation(fb.getLocation());
            dto.setFeedback(fbDto);
            dto.setHasFeedback(true);
        });

        return dto;
    }

    private DuplicateMatch findPossibleDuplicate(Complaint complaint) {
        if (complaint.getDepartment() == null) {
            return null;
        }

        return complaintRepository.findByDepartmentOrderByCreatedAtDesc(complaint.getDepartment()).stream()
                .filter(candidate -> !candidate.getComplaintId().equals(complaint.getComplaintId()))
                .filter(candidate -> candidate.getDuplicateStatus() != Complaint.DuplicateStatus.DUPLICATE)
                .map(candidate -> new DuplicateMatch(candidate,
                        textSimilarity(complaint, candidate),
                        distanceMetres(complaint, candidate)))
                .filter(match -> match.textSimilarity() >= DUPLICATE_TEXT_THRESHOLD
                        && match.distanceMetres() <= DUPLICATE_RADIUS_METRES)
                .max((left, right) -> Double.compare(left.textSimilarity(), right.textSimilarity()))
                .orElse(null);
    }

    private double textSimilarity(Complaint first, Complaint second) {
        String firstText = normalizeText(first.getTitle() + " " + first.getDescription());
        String secondText = normalizeText(second.getTitle() + " " + second.getDescription());
        List<String> firstTokens = List.of(firstText.split(" "));
        List<String> secondTokens = List.of(secondText.split(" "));
        List<String> vocabulary = firstTokens.stream().filter(token -> !token.isBlank()).distinct().collect(Collectors.toList());
        secondTokens.stream().filter(token -> !token.isBlank()).filter(token -> !vocabulary.contains(token)).distinct().forEach(vocabulary::add);

        double[] firstVector = tfidfVector(firstTokens, secondTokens, vocabulary);
        double[] secondVector = tfidfVector(secondTokens, firstTokens, vocabulary);
        double dot = 0;
        double firstMagnitude = 0;
        double secondMagnitude = 0;
        for (int index = 0; index < vocabulary.size(); index++) {
            dot += firstVector[index] * secondVector[index];
            firstMagnitude += firstVector[index] * firstVector[index];
            secondMagnitude += secondVector[index] * secondVector[index];
        }
        if (firstMagnitude == 0 || secondMagnitude == 0) {
            return 0;
        }
        return dot / (Math.sqrt(firstMagnitude) * Math.sqrt(secondMagnitude));
    }

    private double[] tfidfVector(List<String> document, List<String> otherDocument, List<String> vocabulary) {
        double[] vector = new double[vocabulary.size()];
        for (int index = 0; index < vocabulary.size(); index++) {
            String term = vocabulary.get(index);
            long termFrequency = document.stream().filter(term::equals).count();
            long documentFrequency = (document.contains(term) ? 1 : 0) + (otherDocument.contains(term) ? 1 : 0);
            double termFrequencyScore = termFrequency / (double) Math.max(document.size(), 1);
            double inverseDocumentFrequency = Math.log(3.0 / (documentFrequency + 1.0)) + 1.0;
            vector[index] = termFrequencyScore * inverseDocumentFrequency;
        }
        return vector;
    }

    private String normalizeText(String text) {
        return text.toLowerCase().replaceAll("[^\\p{L}\\p{N}\\s]", " ").replaceAll("\\s+", " ").trim();
    }

    private double distanceMetres(Complaint first, Complaint second) {
        if (first.getLatitude() == null || first.getLongitude() == null
                || second.getLatitude() == null || second.getLongitude() == null) {
            return Double.MAX_VALUE;
        }
        double earthRadiusMetres = 6_371_000;
        double latitudeDifference = Math.toRadians(second.getLatitude() - first.getLatitude());
        double longitudeDifference = Math.toRadians(second.getLongitude() - first.getLongitude());
        double latitudeOne = Math.toRadians(first.getLatitude());
        double latitudeTwo = Math.toRadians(second.getLatitude());
        double haversine = Math.sin(latitudeDifference / 2) * Math.sin(latitudeDifference / 2)
                + Math.cos(latitudeOne) * Math.cos(latitudeTwo)
                * Math.sin(longitudeDifference / 2) * Math.sin(longitudeDifference / 2);
        return earthRadiusMetres * 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
    }

    private double roundPercentage(double similarity) {
        return Math.round(similarity * 10000.0) / 100.0;
    }

    private double roundDistance(double distance) {
        return Math.round(distance * 10.0) / 10.0;
    }

    private record DuplicateMatch(Complaint complaint, double textSimilarity, double distanceMetres) {
    }
}
