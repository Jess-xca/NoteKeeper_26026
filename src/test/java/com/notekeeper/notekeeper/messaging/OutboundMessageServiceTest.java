package com.notekeeper.notekeeper.messaging;

import com.notekeeper.notekeeper.service.SystemSettingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutboundMessageServiceTest {

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private SystemSettingService systemSettingService;

    @InjectMocks
    private OutboundMessageService outboundMessageService;

    @BeforeEach
    void notificationsOnByDefault() {
        lenient().when(systemSettingService.isEmailEnabled()).thenReturn(true);
        lenient().when(systemSettingService.isSmsEnabled()).thenReturn(true);
    }

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

    @Test
    void invitationEmailNamesInviterWorkspaceAndRole() {
        outboundMessageService.sendInvitationEmail("sam@example.com", "Ada Lovelace", "Team Notes", "EDITOR");

        AppEvent event = capture();
        assertEquals("email", event.getType());
        assertEquals("sam@example.com", event.getRecipient());
        assertTrue(event.getBody().contains("Ada Lovelace"));
        assertTrue(event.getBody().contains("Team Notes"));
        assertTrue(event.getBody().contains("editor"));
    }

    @Test
    void shareAndInvitationEmailsAreSkippedWhenAdminTurnsEmailOff() {
        lenient().when(systemSettingService.isEmailEnabled()).thenReturn(false);

        outboundMessageService.sendShareNotification("sam@example.com", "Ada", "Plan", "Page");
        outboundMessageService.sendInvitationEmail("sam@example.com", "Ada", "Team Notes", "VIEWER");

        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void securityEmailsStillSendWhenAdminTurnsEmailOff() {
        lenient().when(systemSettingService.isEmailEnabled()).thenReturn(false);

        outboundMessageService.send2FACode("ada@example.com", "654321");

        assertEquals("ada@example.com", capture().getRecipient());
    }

    @Test
    void smsIsSkippedWhenAdminTurnsSmsOff() {
        lenient().when(systemSettingService.isSmsEnabled()).thenReturn(false);

        outboundMessageService.sendSms("+250700000000", "hello");

        verify(eventPublisher, never()).publish(any());
    }

    private AppEvent capture() {
        ArgumentCaptor<AppEvent> captor = ArgumentCaptor.forClass(AppEvent.class);
        verify(eventPublisher).publish(captor.capture());
        return captor.getValue();
    }
}
