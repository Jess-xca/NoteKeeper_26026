package com.notekeeper.notekeeper.messaging;

public interface EventPublisher {

    void publish(AppEvent event);
}
