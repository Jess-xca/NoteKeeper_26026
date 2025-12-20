package com.notekeeper.notekeeper.controller;

import com.notekeeper.notekeeper.dto.NotificationDTO;
import com.notekeeper.notekeeper.mapper.DTOMapper;
import com.notekeeper.notekeeper.model.Notification;
import com.notekeeper.notekeeper.model.NotificationType;
import com.notekeeper.notekeeper.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private DTOMapper dtoMapper;

    @PostMapping
    public ResponseEntity<NotificationDTO> createNotification(
            @RequestParam String userId,
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam NotificationType type) {
        String resultId = notificationService.createNotification(userId, title, message, type);
        Notification notification = notificationService.getNotificationById(resultId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.toNotificationDTO(notification));
    }

    @GetMapping("/my")
    public ResponseEntity<List<NotificationDTO>> getMyNotifications(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.notekeeper.notekeeper.security.UserPrincipal principal) {
        List<Notification> notifications = notificationService.getAllNotifications(principal.getId());
        List<NotificationDTO> notificationDTOs = notifications.stream()
                .map(dtoMapper::toNotificationDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(notificationDTOs);
    }

    @GetMapping("/my/unread")
    public ResponseEntity<List<NotificationDTO>> getMyUnreadNotifications(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.notekeeper.notekeeper.security.UserPrincipal principal) {
        List<Notification> notifications = notificationService.getUnreadNotifications(principal.getId());
        List<NotificationDTO> notificationDTOs = notifications.stream()
                .map(dtoMapper::toNotificationDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(notificationDTOs);
    }

    @GetMapping("/my/count")
    public ResponseEntity<Long> countMyUnreadNotifications(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.notekeeper.notekeeper.security.UserPrincipal principal) {
        long count = notificationService.countUnreadNotifications(principal.getId());
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{id}/mark-read")
    public ResponseEntity<java.util.Map<String, String>> markAsRead(@PathVariable String id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(java.util.Map.of("message", "Notification marked as read"));
    }

    @PutMapping("/{id}/mark-unread")
    public ResponseEntity<java.util.Map<String, String>> markAsUnread(@PathVariable String id) {
        notificationService.markAsUnread(id);
        return ResponseEntity.ok(java.util.Map.of("message", "Notification marked as unread"));
    }

    @PutMapping("/user/{userId}/mark-all-read")
    public ResponseEntity<java.util.Map<String, String>> markAllAsRead(@PathVariable String userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(java.util.Map.of("message", "All notifications marked as read"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<java.util.Map<String, String>> deleteNotification(@PathVariable String id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(java.util.Map.of("message", "Notification deleted successfully"));
    }
}
