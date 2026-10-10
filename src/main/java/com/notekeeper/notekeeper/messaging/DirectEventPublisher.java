package com.notekeeper.notekeeper.messaging;

import com.notekeeper.notekeeper.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!docker")
public class DirectEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(DirectEventPublisher.class);

    private final EmailService emailService;

    public DirectEventPublisher(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void publish(AppEvent event) {
        if ("email".equals(event.getType())) {
            emailService.sendEmail(event.getRecipient(), event.getSubject(), event.getBody());
            return;
        }
        log.info("Recorded {} event for {} without a broker", event.getType(), event.getRecipient());
    }
}
