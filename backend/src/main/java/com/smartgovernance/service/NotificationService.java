package com.smartgovernance.service;

import com.smartgovernance.dto.NotificationDto;
import com.smartgovernance.entity.Citizen;
import com.smartgovernance.entity.Complaint;
import com.smartgovernance.entity.Notification;
import com.smartgovernance.repository.CitizenRepository;
import com.smartgovernance.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final CitizenRepository citizenRepository;

    public NotificationService(NotificationRepository notificationRepository, CitizenRepository citizenRepository) {
        this.notificationRepository = notificationRepository;
        this.citizenRepository = citizenRepository;
    }

    @Transactional
    public void sendNotification(Citizen citizen, Complaint complaint, String title, String message, String type) {
        if (citizen == null) {
            logger.warn("Cannot send notification without a valid citizen.");
            return;
        }
        Notification notification = new Notification(citizen, complaint, title, message, type);
        notificationRepository.save(notification);
        logger.info("Notification [{}] created for citizen ID: {}", type, citizen.getCitizenId());
    }

    @Transactional
    public void sendNotification(Citizen citizen, String title, String message, String type, Complaint complaint) {
        sendNotification(citizen, complaint, title, message, type);
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getNotificationsForCitizen(Long citizenId, Boolean unreadOnly) {
        Citizen citizen = citizenRepository.findById(citizenId)
                .orElseThrow(() -> new IllegalArgumentException("Citizen not found."));

        List<Notification> list;
        if (Boolean.TRUE.equals(unreadOnly)) {
            list = notificationRepository.findByCitizenAndIsReadOrderByCreatedAtDesc(citizen, false);
        } else {
            list = notificationRepository.findByCitizenOrderByCreatedAtDesc(citizen);
        }

        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long citizenId) {
        Citizen citizen = citizenRepository.findById(citizenId)
                .orElseThrow(() -> new IllegalArgumentException("Citizen not found."));
        return notificationRepository.countByCitizenAndIsRead(citizen, false);
    }

    @Transactional
    public NotificationDto markAsRead(Long citizenId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found."));

        if (!notification.getCitizen().getCitizenId().equals(citizenId)) {
            throw new IllegalArgumentException("Unauthorized to access this notification.");
        }

        notification.setRead(true);
        Notification saved = notificationRepository.save(notification);
        return toDto(saved);
    }

    @Transactional
    public int markAllAsRead(Long citizenId) {
        Citizen citizen = citizenRepository.findById(citizenId)
                .orElseThrow(() -> new IllegalArgumentException("Citizen not found."));
        return notificationRepository.markAllAsReadForCitizen(citizen);
    }

    @Transactional
    public void deleteNotification(Long citizenId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found."));

        if (!notification.getCitizen().getCitizenId().equals(citizenId)) {
            throw new IllegalArgumentException("Unauthorized to access this notification.");
        }

        notificationRepository.delete(notification);
    }

    private NotificationDto toDto(Notification n) {
        String complaintId = n.getComplaint() != null ? n.getComplaint().getComplaintId() : null;
        String complaintTitle = n.getComplaint() != null ? n.getComplaint().getTitle() : null;
        return new NotificationDto(
                n.getNotificationId(),
                n.getCitizen().getCitizenId(),
                complaintId,
                complaintTitle,
                n.getTitle(),
                n.getMessage(),
                n.getType(),
                n.isRead(),
                n.getCreatedAt()
        );
    }
}
