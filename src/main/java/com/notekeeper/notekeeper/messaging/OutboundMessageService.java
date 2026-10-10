package com.notekeeper.notekeeper.messaging;

import org.springframework.stereotype.Service;

@Service
public class OutboundMessageService {

    private final EventPublisher eventPublisher;

    public OutboundMessageService(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void sendShareNotification(String toEmail, String shareByName, String contentName, String type) {
        String subject = "Notekeeper: New " + type + " shared with you";
        String body = String.format(
                "Hello,%n%n%s has shared a %s with you: \"%s\".%n%nLogin to Notekeeper to view it.%n%nBest regards,%nThe Notekeeper Team",
                shareByName, type.toLowerCase(), contentName);
        eventPublisher.publish(AppEvent.email(toEmail, subject, body));
    }

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        String resetUrl = "http://localhost:3000/reset-password/" + resetToken;
        String body = "Hello,\n\n"
                + "You requested to reset your password for NoteKeeper.\n\n"
                + "Click the link below to reset your password:\n"
                + resetUrl + "\n\n"
                + "This link will expire in 1 hour.\n\n"
                + "If you didn't request this, please ignore this email.\n\n"
                + "Best regards,\n"
                + "NoteKeeper Team";
        eventPublisher.publish(AppEvent.email(toEmail, "Password Reset Request - NoteKeeper", body));
    }

    public void send2FACode(String toEmail, String code) {
        String body = "Hello,\n\n"
                + "Your two-factor authentication code is:\n\n"
                + code + "\n\n"
                + "This code will expire in 5 minutes.\n\n"
                + "Best regards,\n"
                + "NoteKeeper Team";
        eventPublisher.publish(AppEvent.email(toEmail, "Your 2FA Code - NoteKeeper", body));
    }

    public void sendSms(String phoneNumber, String body) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return;
        }
        eventPublisher.publish(AppEvent.sms(phoneNumber, body));
    }

    public void publishAppEvent(String name, String body) {
        eventPublisher.publish(AppEvent.app(name, body));
    }
}
