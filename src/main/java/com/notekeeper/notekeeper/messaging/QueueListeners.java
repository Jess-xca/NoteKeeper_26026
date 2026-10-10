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
