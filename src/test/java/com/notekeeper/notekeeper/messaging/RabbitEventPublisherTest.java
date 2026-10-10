package com.notekeeper.notekeeper.messaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void publishesToTheQueueNamedByTheEvent() {
        RabbitEventPublisher publisher = new RabbitEventPublisher(rabbitTemplate);
        AppEvent event = AppEvent.email("ada@example.com", "Subject", "Body");

        publisher.publish(event);

        verify(rabbitTemplate).convertAndSend("email", event);
    }
}
