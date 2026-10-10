package com.notekeeper.notekeeper.messaging;

import java.io.Serializable;

public class AppEvent implements Serializable {

    private String type;
    private String recipient;
    private String subject;
    private String body;

    public AppEvent() {
    }

    public AppEvent(String type, String recipient, String subject, String body) {
        this.type = type;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
    }

    public static AppEvent email(String recipient, String subject, String body) {
        return new AppEvent("email", recipient, subject, body);
    }

    public static AppEvent sms(String recipient, String body) {
        return new AppEvent("sms", recipient, null, body);
    }

    public static AppEvent app(String name, String body) {
        return new AppEvent("app", name, name, body);
    }

    public String queueName() {
        return switch (type) {
            case "email" -> "email";
            case "sms" -> "sms";
            case "app" -> "app-events";
            default -> throw new IllegalArgumentException("Unknown event type: " + type);
        };
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
