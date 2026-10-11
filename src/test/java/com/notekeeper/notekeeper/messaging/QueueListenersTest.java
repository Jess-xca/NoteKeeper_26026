package com.notekeeper.notekeeper.messaging;

import com.notekeeper.notekeeper.document.DeliveryLog;
import com.notekeeper.notekeeper.repository.mongo.DeliveryLogRepository;
import com.notekeeper.notekeeper.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class QueueListenersTest {

    @Mock
    private EmailService emailService;

    @Mock
    private DeliveryLogRepository deliveryLogRepository;

    @InjectMocks
    private QueueListeners listeners;

    @Test
    void emailIsSentThroughSmtpAndLoggedWithoutItsBody() {
        listeners.onEmail(AppEvent.email("ada@example.com", "Your 2FA Code", "secret body 123456"));

        verify(emailService).sendEmail("ada@example.com", "Your 2FA Code", "secret body 123456");
        DeliveryLog saved = captureLog();
        assertEquals("email", saved.getChannel());
        assertEquals("ada@example.com", saved.getRecipient());
        assertEquals("Your 2FA Code", saved.getBody());
        assertFalse(saved.getBody().contains("123456"));
    }

    @Test
    void aMongoFailureDoesNotMakeRabbitRedeliverAnEmailThatWasAlreadySent() {
        doThrow(new RuntimeException("mongo down")).when(deliveryLogRepository).save(any());

        listeners.onEmail(AppEvent.email("ada@example.com", "Hello", "Body"));

        verify(emailService).sendEmail("ada@example.com", "Hello", "Body");
    }

    @Test
    void smsIsStoredInTheDeliveryLog() {
        listeners.onSms(AppEvent.sms("+250700000000", "A page was shared with you"));

        DeliveryLog saved = captureLog();
        assertEquals("sms", saved.getChannel());
        assertEquals("+250700000000", saved.getRecipient());
        assertEquals("A page was shared with you", saved.getBody());
    }

    @Test
    void appEventIsStoredInTheDeliveryLog() {
        listeners.onAppEvent(AppEvent.app("page-shared", "pageId=1"));

        DeliveryLog saved = captureLog();
        assertEquals("app", saved.getChannel());
        assertEquals("page-shared", saved.getRecipient());
        assertEquals("pageId=1", saved.getBody());
    }

    private DeliveryLog captureLog() {
        ArgumentCaptor<DeliveryLog> captor = ArgumentCaptor.forClass(DeliveryLog.class);
        verify(deliveryLogRepository).save(captor.capture());
        return captor.getValue();
    }
}
