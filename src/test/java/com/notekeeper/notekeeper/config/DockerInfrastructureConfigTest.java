package com.notekeeper.notekeeper.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DockerInfrastructureConfigTest {

    private final DockerInfrastructureConfig config = new DockerInfrastructureConfig();

    @Test
    void thereIsARabbitAdminSoTheQueuesAreCreatedOnTheBroker() {
        // Without this bean the queues are never declared and the listeners fail with NOT_FOUND.
        assertNotNull(config.rabbitAdmin(new CachingConnectionFactory("localhost", 5672)));
    }

    @Test
    void theThreeQueuesAreDurableAndNamedAfterTheEventTypes() {
        assertEquals("email", config.emailQueue().getName());
        assertEquals("sms", config.smsQueue().getName());
        assertEquals("app-events", config.appEventsQueue().getName());
        assertTrue(config.emailQueue().isDurable());
        assertTrue(config.smsQueue().isDurable());
        assertTrue(config.appEventsQueue().isDurable());
    }
}
