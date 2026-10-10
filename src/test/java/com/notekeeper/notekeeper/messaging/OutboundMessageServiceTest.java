package com.notekeeper.notekeeper.messaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutboundMessageServiceTest {

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private OutboundMessageService outboundMessageService;

    @Test
    void emailEventHasRecipientAndBody() {
        outboundMessageService.send2FACode("ada@example.com", "123456");

        AppEvent event = capture();
        assertEquals("email", event.getType());
        assertEquals("ada@example.com", event.getRecipient());
        assertTrue(event.getBody().contains("123456"));
        assertEquals("email", event.queueName());
    }

    @Test
    void smsEventHasRecipientAndBody() {
        outboundMessageService.sendSms("+250700000000", "A page was shared with you");

        AppEvent event = capture();
        assertEquals("sms", event.getType());
        assertEquals("+250700000000", event.getRecipient());
        assertEquals("A page was shared with you", event.getBody());
        assertEquals("sms", event.queueName());
    }

    @Test
    void appEventHasNameAndBody() {
        outboundMessageService.publishAppEvent("page-shared", "pageId=1");

        AppEvent event = capture();
        assertEquals("app", event.getType());
        assertEquals("page-shared", event.getRecipient());
        assertEquals("pageId=1", event.getBody());
        assertEquals("app-events", event.queueName());
    }

    private AppEvent capture() {
        ArgumentCaptor<AppEvent> captor = ArgumentCaptor.forClass(AppEvent.class);
        verify(eventPublisher).publish(captor.capture());
        return captor.getValue();
    }
}
