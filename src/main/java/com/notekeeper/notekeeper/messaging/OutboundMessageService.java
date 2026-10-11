package com.notekeeper.notekeeper.messaging;

import com.notekeeper.notekeeper.service.SystemSettingService;
import org.springframework.stereotype.Service;

@Service
public class OutboundMessageService {

    private final EventPublisher eventPublisher;
    private final SystemSettingService systemSettingService;

    public OutboundMessageService(EventPublisher eventPublisher, SystemSettingService systemSettingService) {
        this.eventPublisher = eventPublisher;
        this.systemSettingService = systemSettingService;
    }

    /**
     * Share and invitation emails can be switched off by an admin. Security emails
     * (2FA code, password reset) are never affected by that switch.
     */
    public void sendShareNotification(String toEmail, String shareByName, String contentName, String type) {
        if (!systemSettingService.isEmailEnabled()) {
            return;
        }
        String subject = "Notekeeper: New " + type + " shared with you";
        String body = String.format(
                "Hello,%n%n%s has shared a %s with you: \"%s\".%n%nLogin to Notekeeper to view it.%n%nBest regards,%nThe Notekeeper Team",
                shareByName, type.toLowerCase(), contentName);
        eventPublisher.publish(AppEvent.email(toEmail, subject, body));
    }

    public void sendInvitationEmail(String toEmail, String inviterName, String workspaceName, String role) {
        if (!systemSettingService.isEmailEnabled()) {
            return;
        }
        String subject = "Notekeeper: " + inviterName + " invited you to a workspace";
        String body = String.format(
                "Hello,%n%n%s invited you to join the workspace \"%s\" as %s.%n%n"
                        + "Open Notekeeper and use the bell icon to accept or decline the invitation.%n%n"
                        + "Best regards,%nThe Notekeeper Team",
                inviterName, workspaceName, role.toLowerCase());
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
        if (!systemSettingService.isSmsEnabled()) {
            return;
        }
        eventPublisher.publish(AppEvent.sms(phoneNumber, body));
    }

    public void publishAppEvent(String name, String body) {
        eventPublisher.publish(AppEvent.app(name, body));
    }
}
