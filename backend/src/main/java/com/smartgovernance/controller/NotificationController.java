package com.smartgovernance.controller;

import com.smartgovernance.dto.ApiResponse;
import com.smartgovernance.dto.NotificationDto;
import com.smartgovernance.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getNotifications(
            @RequestParam(required = false) Boolean unreadOnly,
            HttpSession session) {
        Long citizenId = requireCitizenId(session);
        List<NotificationDto> notifications = notificationService.getNotificationsForCitizen(citizenId, unreadOnly);
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved", notifications));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse> getUnreadCount(HttpSession session) {
        Long citizenId = requireCitizenId(session);
        long count = notificationService.getUnreadCount(citizenId);
        return ResponseEntity.ok(ApiResponse.success("Unread count retrieved", Map.of("unreadCount", count)));
    }

    @RequestMapping(value = "/{notificationId}/read", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<ApiResponse> markAsRead(
            @PathVariable Long notificationId,
            HttpSession session) {
        Long citizenId = requireCitizenId(session);
        NotificationDto updated = notificationService.markAsRead(citizenId, notificationId);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", updated));
    }

    @RequestMapping(value = "/read-all", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<ApiResponse> markAllAsRead(HttpSession session) {
        Long citizenId = requireCitizenId(session);
        int updated = notificationService.markAllAsRead(citizenId);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", Map.of("count", updated)));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse> deleteNotification(
            @PathVariable Long notificationId,
            HttpSession session) {
        Long citizenId = requireCitizenId(session);
        notificationService.deleteNotification(citizenId, notificationId);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted", null));
    }

    private Long requireCitizenId(HttpSession session) {
        String userType = (String) session.getAttribute("userType");
        if (!"CITIZEN".equalsIgnoreCase(userType)) {
            throw new IllegalArgumentException("Only citizens can access notifications.");
        }
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            throw new IllegalArgumentException("Citizen session is missing.");
        }
        return userId;
    }
}
