package com.notekeeper.notekeeper.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "delivery_logs")
public class DeliveryLog {

    @Id
    private String id;

    @Indexed
    private String channel;

    private String recipient;
    private String body;
    private LocalDateTime recordedAt;

    public DeliveryLog() {
    }

    public DeliveryLog(String channel, String recipient, String body) {
        this.channel = channel;
        this.recipient = recipient;
        this.body = body;
        this.recordedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getChannel() {
        return channel;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getBody() {
        return body;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }
}
