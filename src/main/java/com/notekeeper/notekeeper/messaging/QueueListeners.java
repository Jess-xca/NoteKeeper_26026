package com.notekeeper.notekeeper.messaging;

import com.notekeeper.notekeeper.document.DeliveryLog;
import com.notekeeper.notekeeper.repository.mongo.DeliveryLogRepository;
import com.notekeeper.notekeeper.service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("docker")
public class QueueListeners {

    private final EmailService emailService;
    private final DeliveryLogRepository deliveryLogRepository;

    public QueueListeners(EmailService emailService, DeliveryLogRepository deliveryLogRepository) {
        this.emailService = emailService;
        this.deliveryLogRepository = deliveryLogRepository;
    }

    @RabbitListener(queues = "email")
    public void onEmail(AppEvent event) {
        emailService.sendEmail(event.getRecipient(), event.getSubject(), event.getBody());
        // Only the subject is kept: e-mail bodies can hold sign-in codes and reset links.
        try {
            deliveryLogRepository.save(new DeliveryLog("email", event.getRecipient(), event.getSubject()));
        } catch (Exception e) {
            // The mail has already gone out. A logging problem must not make RabbitMQ redeliver it.
        }
    }

    @RabbitListener(queues = "sms")
    public void onSms(AppEvent event) {
        deliveryLogRepository.save(new DeliveryLog("sms", event.getRecipient(), event.getBody()));
    }

    @RabbitListener(queues = "app-events")
    public void onAppEvent(AppEvent event) {
        deliveryLogRepository.save(new DeliveryLog("app", event.getRecipient(), event.getBody()));
    }
}
