package com.notekeeper.notekeeper.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Reset Request - NoteKeeper");
            
            String resetUrl = "http://localhost:3000/reset-password/" + resetToken;
            String emailBody = "Hello,\n\n" +
                    "You requested to reset your password for NoteKeeper.\n\n" +
                    "Click the link below to reset your password:\n" +
                    resetUrl + "\n\n" +
                    "This link will expire in 1 hour.\n\n" +
                    "If you didn't request this, please ignore this email.\n\n" +
                    "Best regards,\n" +
                    "NoteKeeper Team";
            
            message.setText(emailBody);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }

    public void send2FACode(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your 2FA Code - NoteKeeper");
            
            String emailBody = "Hello,\n\n" +
                    "Your two-factor authentication code is:\n\n" +
                    code + "\n\n" +
                    "This code will expire in 5 minutes.\n\n" +
                    "Best regards,\n" +
                    "NoteKeeper Team";
            
            message.setText(emailBody);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send 2FA email: " + e.getMessage());
        }
    }

    public void sendWelcomeEmail(String toEmail, String firstName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to NoteKeeper!");
            
            String emailBody = "Hello " + firstName + ",\n\n" +
                    "Welcome to NoteKeeper!\n\n" +
                    "Your account has been successfully created.\n\n" +
                    "Start organizing your notes today!\n\n" +
                    "Best regards,\n" +
                    "NoteKeeper Team";
            
            message.setText(emailBody);
            mailSender.send(message);
        } catch (Exception e) {
            // Don't fail registration if welcome email fails
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }
    }
}
