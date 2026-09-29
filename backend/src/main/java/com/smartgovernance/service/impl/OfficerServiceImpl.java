package com.smartgovernance.service.impl;

import com.smartgovernance.dto.*;
import com.smartgovernance.entity.Complaint;
import com.smartgovernance.entity.ComplaintHistory;
import com.smartgovernance.entity.Escalation;
import com.smartgovernance.entity.Officer;
import com.smartgovernance.entity.ReopenRequest;
import com.smartgovernance.entity.Feedback;
import com.smartgovernance.repository.ComplaintHistoryRepository;
import com.smartgovernance.repository.ComplaintRepository;
import com.smartgovernance.repository.EscalationRepository;
import com.smartgovernance.repository.FeedbackRepository;
import com.smartgovernance.repository.OfficerRepository;
import com.smartgovernance.repository.ReopenRequestRepository;
import com.smartgovernance.service.NotificationService;
import com.smartgovernance.service.OfficerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OfficerServiceImpl implements OfficerService {

    private static final double DUPLICATE_TEXT_THRESHOLD = 0.70;
    private static final double DUPLICATE_RADIUS_METRES = 500.0;

    private final ComplaintRepository complaintRepository;
    private final OfficerRepository officerRepository;
    private final ComplaintHistoryRepository complaintHistoryRepository;
    private final ReopenRequestRepository reopenRequestRepository;
    private final EscalationRepository escalationRepository;
    private final FeedbackRepository feedbackRepository;
    private final NotificationService notificationService;

    public OfficerServiceImpl(ComplaintRepository complaintRepository,
                               OfficerRepository officerRepository,
                               ComplaintHistoryRepository complaintHistoryRepository,
                               ReopenRequestRepository reopenRequestRepository,
                               EscalationRepository escalationRepository,
                               FeedbackRepository feedbackRepository,
                               NotificationService notificationService) {
        this.complaintRepository = complaintRepository;
        this.officerRepository = officerRepository;
        this.complaintHistoryRepository = complaintHistoryRepository;
        this.reopenRequestRepository = reopenRequestRepository;
        this.escalationRepository = escalationRepository;
        this.feedbackRepository = feedbackRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public OfficerDashboardDto getDashboard(Long officerId) {
        Officer officer = requireOfficer(officerId);
        List<Complaint> assigned = assignedComplaints(officer);

        long pending = assigned.stream().filter(c -> c.getStatus() == Complaint.ComplaintStatus.APPLIED
                || c.getStatus() == Complaint.ComplaintStatus.COMPLAINT_VIEWED).count();
        long reopened = assigned.stream().filter(c -> c.getStatus() == Complaint.ComplaintStatus.REOPENED).count();
        long inProgress = assigned.stream().filter(c -> c.getStatus() == Complaint.ComplaintStatus.ACTION_TO_BE_TAKEN).count();
        long resolved = assigned.stream().filter(c -> c.getStatus() == Complaint.ComplaintStatus.COMPLAINT_RESOLVED).count();
        long rejected = assigned.stream().filter(c -> c.getStatus() == Complaint.ComplaintStatus.REJECTED).count();
        long duplicate = assigned.stream().filter(c -> c.getStatus() == Complaint.ComplaintStatus.DUPLICATE).count();
        long escalated = escalatedComplaintIds(officer, assigned).size();
        long possibleDuplicates = possibleDuplicateCandidates(officer).size();
        long emergencyPending = assigned.stream().filter(c -> c.getComplaintType() == Complaint.ComplaintType.EMERGENCY
                && (c.getStatus() == Complaint.ComplaintStatus.APPLIED
                || c.getStatus() == Complaint.ComplaintStatus.COMPLAINT_VIEWED
                || c.getStatus() == Complaint.ComplaintStatus.REOPENED)).count();

        OfficerDashboardDto dto = new OfficerDashboardDto();
        dto.setOfficerName(officer.getFullName());
        dto.setDepartment(officer.getDepartment());
        dto.setEmployeeId(officer.getEmployeeId());
        dto.setTotalCount(assigned.size());
        dto.setPendingCount(pending);
        dto.setReopenedCount(reopened);
        dto.setInProgressCount(inProgress);
        dto.setResolvedCount(resolved);
        dto.setRejectedCount(rejected);
        dto.setDuplicateCount(duplicate);
        dto.setEscalatedCount(escalated);
        dto.setPendingDuplicateCount(possibleDuplicates);
        dto.setEmergencyPendingCount(emergencyPending);
        dto.setAttentionCount(pending + reopened + possibleDuplicates + emergencyPending);
        dto.setRecentComplaints(assigned.stream().limit(4).map(this::toSummary).collect(Collectors.toList()));
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfficerComplaintSummaryDto> getMyComplaints(Long officerId) {
        return assignedComplaints(requireOfficer(officerId)).stream().map(this::toSummary).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OfficerComplaintDetailDto getMyComplaintById(Long officerId, String complaintId) {
        Officer officer = requireOfficer(officerId);
        return toDetail(requireOwnedComplaint(officer, complaintId), officer);
    }

    @Override
    @Transactional
    public OfficerComplaintDetailDto updateMyComplaintStatus(Long officerId, String complaintId, OfficerComplaintUpdateRequest request) {
        Officer officer = requireOfficer(officerId);
        Complaint complaint = requireOwnedComplaint(officer, complaintId);
        Complaint.ComplaintStatus previous = complaint.getStatus();
        Complaint.ComplaintStatus next = parseStatus(request.getStatus());

        if (!isValidTransition(previous, next)) {
            throw new IllegalArgumentException("Invalid status transition from " + previous + " to " + next + ".");
        }

        complaint.setStatus(next);
        complaint.setOfficer(officer);
        String remarks = request.getRemarks() == null ? "" : request.getRemarks().trim();
        if (!remarks.isBlank()) complaint.setActionTaken(remarks);
        if (next == Complaint.ComplaintStatus.COMPLAINT_RESOLVED) complaint.setResolvedAt(LocalDateTime.now());
        if (next != Complaint.ComplaintStatus.COMPLAINT_RESOLVED) complaint.setResolvedAt(null);
        complaintRepository.save(complaint);

        String message = remarks.isBlank()
                ? "Status changed from " + previous + " to " + next + " by officer."
                : "Status changed from " + previous + " to " + next + ". Remarks: " + remarks;
        ComplaintHistory history = new ComplaintHistory(complaint, next, message);
        history.setOfficer(officer);
        complaintHistoryRepository.save(history);

        if (complaint.getCitizen() != null) {
            try {
                String type = (next == Complaint.ComplaintStatus.COMPLAINT_RESOLVED) ? "COMPLAINT_RESOLVED" : "STATUS_CHANGED";
                String desc = (next == Complaint.ComplaintStatus.COMPLAINT_RESOLVED)
                        ? "Your complaint (" + complaint.getComplaintId() + ") has been marked as resolved by Officer " + officer.getFullName() + "."
                        : "Your complaint (" + complaint.getComplaintId() + ") status has been updated to " + next + ".";
                if (!remarks.isBlank()) {
                    desc += " Remarks: " + remarks;
                }
                notificationService.sendNotification(
                        complaint.getCitizen(),
                        "Complaint Status: " + next,
                        desc,
                        type,
                        complaint
                );
            } catch (Exception e) {
                // Ignore notification error
            }
        }

        return toDetail(complaint, officer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintHistoryEntryDto> getMyComplaintHistory(Long officerId, String complaintId) {
        Officer officer = requireOfficer(officerId);
        Complaint complaint = requireOwnedComplaint(officer, complaintId);
        List<ComplaintHistory> ascending = new ArrayList<>(complaintHistoryRepository.findByComplaintOrderByCreatedAtDesc(complaint));
        Collections.reverse(ascending);
        List<ComplaintHistoryEntryDto> result = new ArrayList<>();
        Complaint.ComplaintStatus previous = null;
        for (ComplaintHistory entry : ascending) {
            ComplaintHistoryEntryDto dto = new ComplaintHistoryEntryDto();
            dto.setHistoryId(entry.getHistoryId());
            dto.setComplaintId(complaint.getComplaintId());
            dto.setPreviousStatus(previous == null ? null : previous.name());
            dto.setNewStatus(entry.getStatus() == null ? null : entry.getStatus().name());
            dto.setRemarks(entry.getMessage());
            dto.setOfficerName(entry.getOfficer() == null ? null : entry.getOfficer().getFullName());
            dto.setTimestamp(entry.getCreatedAt());
            result.add(dto);
            previous = entry.getStatus();
        }
        Collections.reverse(result);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfficerDuplicateSummaryDto> getDuplicateComplaints(Long officerId) {
        Officer officer = requireOfficer(officerId);
        // Keep verified DUPLICATE complaints visible so officers can open/review them again.
        // Pending candidates are still discovered using the existing rule-based matching logic.
        // The "confirmed" bucket is keyed off the complaint's overall status (the same field
        // the dashboard's Confirmed Duplicates stat and the complaint queue pill both use) so
        // this list can never disagree with those counts.
        return assignedComplaints(officer).stream()
                .filter(c -> c.getDuplicateStatus() == Complaint.DuplicateStatus.PENDING_REVIEW
                        || (c.getStatus() == Complaint.ComplaintStatus.DUPLICATE && c.getDuplicateOf() != null))
                .filter(c -> c.getStatus() == Complaint.ComplaintStatus.DUPLICATE
                        || findBestMatch(c, officer) != null)
                .map(this::toDuplicateSummary)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OfficerDuplicateReviewDto getDuplicateReview(Long officerId, String complaintId) {
        Officer officer = requireOfficer(officerId);
        Complaint current = requireOwnedComplaint(officer, complaintId);
        Complaint original = current.getDuplicateOf();
        Match match = original == null ? findBestMatch(current, officer) : new Match(original, textSimilarity(current, original), locationDistance(current, original));
        if (match == null) throw new IllegalArgumentException("No possible original complaint was found for this complaint.");

        OfficerDuplicateReviewDto dto = new OfficerDuplicateReviewDto();
        dto.setCurrentComplaint(toDetail(current, officer));
        dto.setPossibleOriginal(toDetail(match.complaint(), officer));
        dto.setSimilarity(round(match.similarity() * 100));
        dto.setDistanceMetres(match.distanceMetres() == Double.MAX_VALUE ? null : round(match.distanceMetres()));
        dto.setReason(buildDuplicateReason(current, match.complaint(), match.similarity(), match.distanceMetres()));
        return dto;
    }

    @Override
    @Transactional
    public OfficerComplaintDetailDto verifyDuplicate(Long officerId, String complaintId, ComplaintDuplicateStatusRequest request) {
        Officer officer = requireOfficer(officerId);
        Complaint current = requireOwnedComplaint(officer, complaintId);
        String decision = request.getDuplicateStatus() == null ? "" : request.getDuplicateStatus().trim().toUpperCase();
        if (!decision.equals("DUPLICATE") && !decision.equals("NOT_DUPLICATE")) {
            throw new IllegalArgumentException("Duplicate status must be DUPLICATE or NOT_DUPLICATE.");
        }

        if (decision.equals("DUPLICATE")) {
            Complaint original = null;
            if (request.getOriginalComplaintId() != null && !request.getOriginalComplaintId().isBlank()) {
                original = requireOwnedOrDepartmentComplaint(officer, request.getOriginalComplaintId());
            } else {
                Match match = findBestMatch(current, officer);
                if (match != null) original = match.complaint();
            }
            if (original == null || original.getComplaintId().equals(current.getComplaintId())) {
                throw new IllegalArgumentException("An original complaint is required to confirm a duplicate.");
            }
            if (original.getCreatedAt() != null && current.getCreatedAt() != null && original.getCreatedAt().isAfter(current.getCreatedAt())) {
                throw new IllegalArgumentException("The original complaint must be older than the duplicate complaint.");
            }
            current.setDuplicateOf(original);
            current.setDuplicateStatus(Complaint.DuplicateStatus.DUPLICATE);
            current.setStatus(Complaint.ComplaintStatus.DUPLICATE);
        } else {
            // A complaint whose overall status has already been finalized as DUPLICATE is
            // terminal (the officer status dropdown itself locks once status == DUPLICATE).
            // Silently flipping duplicateStatus back to NOT_DUPLICATE here without also
            // reverting status would leave the two fields disagreeing - the complaint would
            // still count as "confirmed" everywhere that reads status (dashboard stat, queue
            // pill) while disappearing from the duplicates list (which reads duplicateStatus).
            // Block the downgrade instead, matching the same rule already enforced elsewhere
            // in this codebase for terminated duplicates.
            if (current.getStatus() == Complaint.ComplaintStatus.DUPLICATE) {
                throw new IllegalArgumentException("A confirmed duplicate complaint cannot be marked as not-a-duplicate.");
            }
            current.setDuplicateStatus(Complaint.DuplicateStatus.NOT_DUPLICATE);
        }
        current.setOfficer(officer);
        String notes = request.getNotes() == null ? "" : request.getNotes().trim();
        current.setActionTaken(notes.isBlank() ? "Duplicate verification completed by officer." : notes);
        complaintRepository.save(current);

        String message = decision.equals("DUPLICATE")
                ? "Officer confirmed this complaint as DUPLICATE. Original complaint: " + current.getDuplicateOf().getComplaintId()
                : "Officer verified this complaint as NOT_DUPLICATE.";
        if (!notes.isBlank()) message += " Remarks: " + notes;
        ComplaintHistory history = new ComplaintHistory(current, current.getStatus(), message);
        history.setOfficer(officer);
        complaintHistoryRepository.save(history);

        if (decision.equals("DUPLICATE") && current.getCitizen() != null) {
            try {
                notificationService.sendNotification(
                        current.getCitizen(),
                        "Complaint Marked as Duplicate",
                        "Your complaint (" + current.getComplaintId() + ") was verified as a duplicate by the reviewing officer.",
                        "COMPLAINT_MARKED_DUPLICATE",
                        current
                );
            } catch (Exception e) {
                // Ignore notification error
            }
        }

        return toDetail(current, officer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfficerEscalationSummaryDto> getEscalatedComplaints(Long officerId) {
        Officer officer = requireOfficer(officerId);
        List<Complaint> assigned = assignedComplaints(officer);
        Map<String, Escalation> latestByComplaint = new LinkedHashMap<>();
        for (Complaint complaint : assigned) {
            for (Escalation escalation : escalationRepository.findByComplaintOrderByCreatedAtDesc(complaint)) {
                latestByComplaint.putIfAbsent(complaint.getComplaintId(), escalation);
            }
        }
        return latestByComplaint.values().stream().map(this::toEscalationSummary).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OfficerComplaintDetailDto escalateComplaint(Long officerId, String complaintId, EscalationRequest request) {
        Officer officer = requireOfficer(officerId);
        Complaint complaint = requireOwnedComplaint(officer, complaintId);
        if (complaint.getStatus() == Complaint.ComplaintStatus.DUPLICATE) {
            throw new IllegalArgumentException("A duplicate complaint cannot be escalated.");
        }
        int level = request.getEscalationLevel() == null || request.getEscalationLevel() < 1 ? 1 : request.getEscalationLevel();
        Escalation escalation = new Escalation(complaint, officer, request.getReason().trim(), level);
        escalation.setRemarks(request.getRemarks() == null ? null : request.getRemarks().trim());
        escalation.setTarget(request.getTarget() == null ? null : request.getTarget().trim());
        escalation.setStatus("PENDING");
        escalationRepository.save(escalation);

        String message = "Complaint escalated to Level " + level + ": " + request.getReason().trim();
        if (request.getRemarks() != null && !request.getRemarks().isBlank()) message += " Remarks: " + request.getRemarks().trim();
        ComplaintHistory history = new ComplaintHistory(complaint, complaint.getStatus(), message);
        history.setOfficer(officer);
        complaintHistoryRepository.save(history);

        if (complaint.getCitizen() != null) {
            try {
                notificationService.sendNotification(
                        complaint.getCitizen(),
                        "Complaint Escalated",
                        "Your complaint (" + complaint.getComplaintId() + ") has been escalated to Level "
                                + level + (escalation.getTarget() != null ? " (" + escalation.getTarget() + ")" : "")
                                + ". Reason: " + escalation.getReason(),
                        "COMPLAINT_ESCALATED",
                        complaint
                );
            } catch (Exception e) {
                // Ignore notification error
            }
        }

        return toDetail(complaint, officer);
    }

    @Override
    @Transactional
    public OfficerComplaintDetailDto updateEscalationStatus(Long officerId, Long escalationId, String status, String remarks) {
        Officer officer = requireOfficer(officerId);
        Escalation escalation = escalationRepository.findById(escalationId)
                .orElseThrow(() -> new IllegalArgumentException("Escalation not found."));

        Complaint complaint = escalation.getComplaint();
        if (complaint == null) {
            throw new IllegalArgumentException("Escalation is not linked to any complaint.");
        }

        if (complaint.getOfficer() == null || !complaint.getOfficer().getOfficerId().equals(officer.getOfficerId())) {
            throw new IllegalArgumentException("You are not authorized to update this escalation.");
        }

        escalation.setStatus(status != null && !status.isBlank() ? status.trim().toUpperCase() : "RESOLVED");
        if (remarks != null && !remarks.isBlank()) {
            escalation.setRemarks(remarks.trim());
        }
        escalationRepository.save(escalation);

        String msg = "Escalation #" + escalationId + " marked as " + escalation.getStatus() + " by " + officer.getFullName() + ".";
        if (remarks != null && !remarks.isBlank()) {
            msg += " Remarks: " + remarks.trim();
        }
        ComplaintHistory history = new ComplaintHistory(complaint, complaint.getStatus(), msg);
        history.setOfficer(officer);
        complaintHistoryRepository.save(history);

        if (complaint.getCitizen() != null) {
            try {
                notificationService.sendNotification(
                        complaint.getCitizen(),
                        "Escalation Updated",
                        "The escalation on your complaint (" + complaint.getComplaintId() + ") has been marked as " + escalation.getStatus() + ".",
                        "ESCALATION_UPDATED",
                        complaint
                );
            } catch (Exception e) {
                // Ignore notification error
            }
        }

        return toDetail(complaint, officer);
    }

    /**
     * Complaints visible to an officer are all complaints belonging to the
     * officer's department. A complaint may be temporarily unassigned; it
     * must still be visible so the officer can review/claim it.
     */
    private List<Complaint> assignedComplaints(Officer officer) {
        return complaintRepository.findByDepartmentNameOrderByCreatedAtDesc(officer.getDepartment());
    }

    private Officer requireOfficer(Long officerId) {
        if (officerId == null) throw new IllegalArgumentException("Officer session is missing.");
        return officerRepository.findById(officerId).orElseThrow(() -> new IllegalArgumentException("Officer not found."));
    }

    private Complaint requireOwnedComplaint(Officer officer, String complaintId) {
        Complaint complaint = complaintRepository.findComplaintWithDetails(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found."));
        if (complaint.getDepartment() == null
                || !complaint.getDepartment().getName().equalsIgnoreCase(officer.getDepartment())) {
            throw new IllegalArgumentException("You are not authorized to access this complaint.");
        }
        return complaint;
    }

    private Complaint requireOwnedOrDepartmentComplaint(Officer officer, String complaintId) {
        Complaint complaint = complaintRepository.findComplaintWithDetails(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Original complaint not found."));
        if (complaint.getDepartment() == null || !complaint.getDepartment().getName().equalsIgnoreCase(officer.getDepartment())) {
            throw new IllegalArgumentException("Complaint belongs to another department.");
        }
        return complaint;
    }

    private Complaint.ComplaintStatus parseStatus(String status) {
        if (status == null) throw new IllegalArgumentException("Status is required.");
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

    private boolean isValidTransition(Complaint.ComplaintStatus current, Complaint.ComplaintStatus next) {
        if (current == Complaint.ComplaintStatus.DUPLICATE) return next == Complaint.ComplaintStatus.DUPLICATE;
        if (next == Complaint.ComplaintStatus.REJECTED) return current != Complaint.ComplaintStatus.COMPLAINT_RESOLVED;
        // An officer can resolve a complaint at any operational stage (APPLIED, COMPLAINT_VIEWED, ACTION_TO_BE_TAKEN, REOPENED)
        if (next == Complaint.ComplaintStatus.COMPLAINT_RESOLVED) return true;
        return (current == Complaint.ComplaintStatus.APPLIED && (next == Complaint.ComplaintStatus.COMPLAINT_VIEWED || next == Complaint.ComplaintStatus.ACTION_TO_BE_TAKEN))
                || (current == Complaint.ComplaintStatus.COMPLAINT_VIEWED && next == Complaint.ComplaintStatus.ACTION_TO_BE_TAKEN)
                || (current == Complaint.ComplaintStatus.REOPENED && (next == Complaint.ComplaintStatus.ACTION_TO_BE_TAKEN || next == Complaint.ComplaintStatus.COMPLAINT_VIEWED))
                || current == next;
    }

    private OfficerComplaintSummaryDto toSummary(Complaint c) {
        OfficerComplaintSummaryDto dto = new OfficerComplaintSummaryDto(c.getComplaintId(), c.getTitle(),
                c.getDepartment() == null ? null : c.getDepartment().getName(),
                c.getStatus() == null ? null : c.getStatus().name(), c.getCreatedAt(), c.getUpdatedAt());
        dto.setComplaintType(c.getComplaintType() == null ? null : c.getComplaintType().name());
        dto.setLocation(c.getLocation());
        dto.setLatitude(c.getLatitude());
        dto.setLongitude(c.getLongitude());
        dto.setAiPriority(c.getAiPriority());
        dto.setAiPhotoMismatchSuspected(c.getAiPhotoMismatchSuspected());
        dto.setAiPhotoMismatchReason(c.getAiPhotoMismatchReason());
        dto.setDuplicateStatus(c.getDuplicateStatus() == null ? null : c.getDuplicateStatus().name());
        Match match = findBestMatch(c, c.getOfficer());
        if (match != null) { dto.setPossibleDuplicateComplaintId(match.complaint().getComplaintId()); dto.setTextSimilarity(round(match.similarity() * 100)); }
        return dto;
    }

    private OfficerComplaintDetailDto toDetail(Complaint c, Officer officer) {
        OfficerComplaintDetailDto dto = new OfficerComplaintDetailDto();
        dto.setComplaintId(c.getComplaintId()); dto.setTitle(c.getTitle()); dto.setDescription(c.getDescription());
        dto.setDepartmentName(c.getDepartment() == null ? null : c.getDepartment().getName());
        dto.setDepartmentId(c.getDepartment() == null ? null : c.getDepartment().getDepartmentId());
        dto.setLocation(c.getLocation()); dto.setStatus(c.getStatus() == null ? null : c.getStatus().name());
        dto.setRemarks(c.getActionTaken()); dto.setCreatedAt(c.getCreatedAt()); dto.setUpdatedAt(c.getUpdatedAt()); dto.setResolvedAt(c.getResolvedAt());
        dto.setComplaintType(c.getComplaintType() == null ? null : c.getComplaintType().name());
        dto.setImageUrl(c.getImageUrl()); dto.setVideoUrl(c.getVideoUrl()); dto.setLatitude(c.getLatitude()); dto.setLongitude(c.getLongitude());
        dto.setDuplicateStatus(c.getDuplicateStatus() == null ? null : c.getDuplicateStatus().name());
        dto.setDuplicateOfComplaintId(c.getDuplicateOf() == null ? null : c.getDuplicateOf().getComplaintId());
        if (c.getCitizen() != null) {
            dto.setCitizenId(c.getCitizen().getCitizenId()); dto.setCitizenName(c.getCitizen().getFullName());
            dto.setCitizenEmail(c.getCitizen().getEmail()); dto.setCitizenPhone(c.getCitizen().getPhoneNumber());
        }
        Match match = c.getDuplicateOf() == null ? findBestMatch(c, officer) : null;
        if (match != null) { dto.setPossibleDuplicateComplaintId(match.complaint().getComplaintId()); dto.setTextSimilarity(round(match.similarity() * 100)); dto.setDistanceMetres(match.distanceMetres() == Double.MAX_VALUE ? null : round(match.distanceMetres())); }
        List<ReopenRequest> reopenRequests = reopenRequestRepository.findByComplaintOrderByCreatedAtDesc(c);
        if (!reopenRequests.isEmpty()) {
            ReopenRequest r = reopenRequests.get(0); OfficerReopenRequestDto rr = new OfficerReopenRequestDto();
            rr.setReopenId(r.getReopenId()); rr.setCitizenId(r.getCitizen() == null ? null : r.getCitizen().getCitizenId());
            rr.setCitizenName(r.getCitizen() == null ? null : r.getCitizen().getFullName()); rr.setReason(r.getReason()); rr.setStatus(r.getStatus()); rr.setCreatedAt(r.getCreatedAt());
            // Surface the citizen's reopen evidence (image/video) so the Officer Side can display it.
            rr.setImageUrl(r.getImageUrl()); rr.setVideoUrl(r.getVideoUrl());
            dto.setReopenRequest(rr);
        }
        List<Escalation> escalations = escalationRepository.findByComplaintOrderByCreatedAtDesc(c);
        if (!escalations.isEmpty()) dto.setLatestEscalation(toEscalationDetail(escalations.get(0)));

        List<Feedback> feedbacks = feedbackRepository.findByComplaintOrderByCreatedAtDesc(c);
        if (feedbacks != null && !feedbacks.isEmpty()) {
            Feedback fb = feedbacks.get(0);
            FeedbackDto fDto = new FeedbackDto(
                    fb.getFeedbackId(),
                    c.getComplaintId(),
                    fb.getCitizen() != null ? fb.getCitizen().getCitizenId() : null,
                    fb.getCitizen() != null ? fb.getCitizen().getFullName() : null,
                    fb.getRating(),
                    fb.getComment(),
                    fb.getImageUrl(),
                    fb.getVideoUrl(),
                    fb.getLocation(),
                    fb.getLatitude(),
                    fb.getLongitude(),
                    fb.getCreatedAt()
            );
            dto.setFeedback(fDto);
        }

        return dto;
    }

    private List<Complaint> possibleDuplicateCandidates(Officer officer) {
        List<Complaint> assigned = assignedComplaints(officer);
        List<Complaint> result = new ArrayList<>();
        for (Complaint c : assigned) {
            if (c.getDuplicateStatus() == Complaint.DuplicateStatus.PENDING_REVIEW && findBestMatch(c, officer) != null) result.add(c);
        }
        return result;
    }

    private Match findBestMatch(Complaint current, Officer officer) {
        if (current == null || current.getDepartment() == null || current.getCreatedAt() == null) return null;
        if (current.getDuplicateStatus() != null && current.getDuplicateStatus() != Complaint.DuplicateStatus.PENDING_REVIEW) return null;
        return complaintRepository.findByDepartmentOrderByCreatedAtDesc(current.getDepartment()).stream()
                .filter(c -> !c.getComplaintId().equals(current.getComplaintId()))
                .filter(c -> c.getCreatedAt() != null && c.getCreatedAt().isBefore(current.getCreatedAt()))
                .filter(c -> c.getDuplicateStatus() != Complaint.DuplicateStatus.DUPLICATE)
                .filter(c -> c.getOfficer() == null || officer == null || c.getOfficer().getOfficerId().equals(officer.getOfficerId()) || c.getDepartment().getName().equalsIgnoreCase(officer.getDepartment()))
                .map(c -> new Match(c, textSimilarity(current, c), locationDistance(current, c)))
                .filter(m -> m.similarity() >= DUPLICATE_TEXT_THRESHOLD && m.distanceMetres() <= DUPLICATE_RADIUS_METRES)
                .max(Comparator.comparingDouble(Match::similarity)).orElse(null);
    }

    private double textSimilarity(Complaint a, Complaint b) {
        List<String> firstTokens = List.of(normalizeText(a.getTitle() + " " + a.getDescription()).split(" "));
        List<String> secondTokens = List.of(normalizeText(b.getTitle() + " " + b.getDescription()).split(" "));
        List<String> vocabulary = firstTokens.stream()
                .filter(token -> !token.isBlank())
                .distinct()
                .collect(Collectors.toList());
        secondTokens.stream()
                .filter(token -> !token.isBlank())
                .filter(token -> !vocabulary.contains(token))
                .distinct()
                .forEach(vocabulary::add);

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
        if (firstMagnitude == 0 || secondMagnitude == 0) return 0;
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

    private double locationDistance(Complaint a, Complaint b) {
        if (a.getLatitude() != null && a.getLongitude() != null && b.getLatitude() != null && b.getLongitude() != null) {
            double earth = 6_371_000; double dLat = Math.toRadians(b.getLatitude() - a.getLatitude()); double dLon = Math.toRadians(b.getLongitude() - a.getLongitude());
            double x = Math.sin(dLat/2)*Math.sin(dLat/2) + Math.cos(Math.toRadians(a.getLatitude()))*Math.cos(Math.toRadians(b.getLatitude()))*Math.sin(dLon/2)*Math.sin(dLon/2);
            return earth * 2 * Math.atan2(Math.sqrt(x), Math.sqrt(1-x));
        }
        return normalize(a.getLocation()).equals(normalize(b.getLocation())) ? 0 : Double.MAX_VALUE;
    }

    private String normalize(String s) { return s == null ? "" : s.toLowerCase().replaceAll("[^\\p{L}\\p{N}\\s]", " ").replaceAll("\\s+", " ").trim(); }

    private String buildDuplicateReason(Complaint a, Complaint b, double similarity, double distance) {
        List<String> reasons = new ArrayList<>();
        reasons.add("Same department");
        reasons.add("Location within " + Math.round(distance) + " metres");
        reasons.add("Similar title/description (" + Math.round(similarity * 100) + "%)");
        return String.join(" • ", reasons);
    }
    private double round(double value) { return Math.round(value * 100.0) / 100.0; }

    private Set<String> escalatedComplaintIds(Officer officer, List<Complaint> assigned) {
        Set<String> ids = new HashSet<>();
        for (Complaint c : assigned) if (!escalationRepository.findByComplaintOrderByCreatedAtDesc(c).isEmpty()) ids.add(c.getComplaintId());
        return ids;
    }

    private OfficerDuplicateSummaryDto toDuplicateSummary(Complaint c) {
        Match match = findBestMatch(c, c.getOfficer()); OfficerDuplicateSummaryDto dto = new OfficerDuplicateSummaryDto();
        dto.setComplaintId(c.getComplaintId()); dto.setTitle(c.getTitle()); dto.setDepartmentName(c.getDepartment()==null?null:c.getDepartment().getName()); dto.setLocation(c.getLocation());
        dto.setStatus(c.getStatus()==null?null:c.getStatus().name());
        // A complaint whose overall status has been finalized as DUPLICATE is always reported
        // as a confirmed duplicate here, even if an older record has a stale duplicateStatus
        // value, so this matches the status-based bucketing used above and the officer never
        // sees a "confirmed" count that doesn't match what the list actually shows.
        String duplicateStatusName = c.getStatus() == Complaint.ComplaintStatus.DUPLICATE
                ? Complaint.DuplicateStatus.DUPLICATE.name()
                : (c.getDuplicateStatus() == null ? null : c.getDuplicateStatus().name());
        dto.setDuplicateStatus(duplicateStatusName);
        dto.setComplaintType(c.getComplaintType()==null?null:c.getComplaintType().name()); dto.setCreatedAt(c.getCreatedAt());
        if (match != null) { dto.setPossibleOriginalId(match.complaint().getComplaintId()); dto.setSimilarity(round(match.similarity()*100)); dto.setDistanceMetres(match.distanceMetres()==Double.MAX_VALUE?null:round(match.distanceMetres())); }
        return dto;
    }

    private OfficerEscalationSummaryDto toEscalationSummary(Escalation e) {
        Complaint c=e.getComplaint(); OfficerEscalationSummaryDto dto=new OfficerEscalationSummaryDto(); dto.setComplaintId(c.getComplaintId()); dto.setTitle(c.getTitle()); dto.setReason(e.getReason()); dto.setComplaintType(c.getComplaintType()==null?null:c.getComplaintType().name()); dto.setEscalationStatus(e.getStatus()); dto.setStatus(c.getStatus()==null?null:c.getStatus().name()); dto.setTarget(e.getTarget()); dto.setOfficerName(e.getEscalatedByOfficer()==null?null:e.getEscalatedByOfficer().getFullName()); dto.setEscalationLevel(e.getEscalationLevel()); dto.setCreatedAt(e.getCreatedAt()); return dto;
    }
    private OfficerEscalationDetailDto toEscalationDetail(Escalation e) { OfficerEscalationDetailDto d=new OfficerEscalationDetailDto(); d.setEscalationId(e.getEscalationId()); d.setReason(e.getReason()); d.setEscalationLevel(e.getEscalationLevel()); d.setStatus(e.getStatus()); d.setRemarks(e.getRemarks()); d.setTarget(e.getTarget()); d.setOfficerName(e.getEscalatedByOfficer()==null?null:e.getEscalatedByOfficer().getFullName()); d.setCreatedAt(e.getCreatedAt()); return d; }

    private record Match(Complaint complaint, double similarity, double distanceMetres) {}
}
