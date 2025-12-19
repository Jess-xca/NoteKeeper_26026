package com.notekeeper.notekeeper.service;

import com.notekeeper.notekeeper.model.Notification;
import com.notekeeper.notekeeper.model.NotificationType;
import com.notekeeper.notekeeper.model.User;
import com.notekeeper.notekeeper.repository.NotificationRepository;
import com.notekeeper.notekeeper.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    // CREATE
    public String createNotification(String userId, String title, String message, NotificationType type) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return "user not found";
        }

        Notification notification = new Notification(userOpt.get(), title, message, type);
        Notification saved = notificationRepository.save(notification);
        return saved.getId();
    }

    // READ
    public Notification getNotificationById(String id) {
        Optional<Notification> notification = notificationRepository.findById(id);
        return notification.orElse(null);
    }

    public List<Notification> getAllNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getUnreadNotifications(String userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    public long countUnreadNotifications(String userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    // UPDATE
    public String markAsRead(String id) {
        Optional<Notification> notificationOpt = notificationRepository.findById(id);

        if (!notificationOpt.isPresent()) {
            return "not found";
        }

        Notification notification = notificationOpt.get();
        notification.setIsRead(true);
        notificationRepository.save(notification);
        return "success";
    }

    public String markAsUnread(String id) {
        Optional<Notification> notificationOpt = notificationRepository.findById(id);

        if (!notificationOpt.isPresent()) {
            return "not found";
        }

        Notification notification = notificationOpt.get();
        notification.setIsRead(false);
        notificationRepository.save(notification);
        return "success";
    }

    public String markAllAsRead(String userId) {
        List<Notification> notifications = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        notifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(notifications);
        return "success";
    }

    // DELETE
    public String deleteNotification(String id) {
        Optional<Notification> notificationOpt = notificationRepository.findById(id);

        if (!notificationOpt.isPresent()) {
            return "not found";
        }

        notificationRepository.delete(notificationOpt.get());
        return "success";
    }
}