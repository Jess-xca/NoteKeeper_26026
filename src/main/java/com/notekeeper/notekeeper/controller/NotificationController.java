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
    public ResponseEntity<?> createNotification(
            @RequestParam String userId,
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam NotificationType type) {
        try {
            String result = notificationService.createNotification(userId, title, message, type);

            if (result.equals("user not found")) {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            } else {
                Notification notification = notificationService.getNotificationById(result);
                return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.toNotificationDTO(notification));
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create notification: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserNotifications(@PathVariable String userId) {
        try {
            List<Notification> notifications = notificationService.getAllNotifications(userId);
            List<NotificationDTO> notificationDTOs = notifications.stream()
                    .map(dtoMapper::toNotificationDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(notificationDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch notifications: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<?> getUnreadNotifications(@PathVariable String userId) {
        try {
            List<Notification> notifications = notificationService.getUnreadNotifications(userId);
            List<NotificationDTO> notificationDTOs = notifications.stream()
                    .map(dtoMapper::toNotificationDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(notificationDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch unread notifications: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/{userId}/count")
    public ResponseEntity<?> countUnreadNotifications(@PathVariable String userId) {
        try {
            long count = notificationService.countUnreadNotifications(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to count notifications: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}/mark-read")
    public ResponseEntity<?> markAsRead(@PathVariable String id) {
        try {
            String result = notificationService.markAsRead(id);

            if (result.equals("not found")) {
                return new ResponseEntity<>("Notification not found", HttpStatus.NOT_FOUND);
            } else {
                return ResponseEntity.ok("Notification marked as read");
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to mark notification: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}/mark-unread")
    public ResponseEntity<?> markAsUnread(@PathVariable String id) {
        try {
            String result = notificationService.markAsUnread(id);

            if (result.equals("not found")) {
                return new ResponseEntity<>("Notification not found", HttpStatus.NOT_FOUND);
            } else {
                return ResponseEntity.ok("Notification marked as unread");
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to mark notification: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/user/{userId}/mark-all-read")
    public ResponseEntity<?> markAllAsRead(@PathVariable String userId) {
        try {
            notificationService.markAllAsRead(userId);
            return ResponseEntity.ok("All notifications marked as read");
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to mark all notifications: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable String id) {
        try {
            String result = notificationService.deleteNotification(id);

            if (result.equals("not found")) {
                return new ResponseEntity<>("Notification not found", HttpStatus.NOT_FOUND);
            } else {
                return ResponseEntity.ok("Notification deleted successfully");
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to delete notification: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
