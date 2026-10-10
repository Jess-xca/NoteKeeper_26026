package com.notekeeper.notekeeper.messaging;

import com.notekeeper.notekeeper.model.Page;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!docker")
public class NoOpPageRevisionRecorder implements PageRevisionRecorder {

    @Override
    public void record(Page page) {
        // Local runs keep the page in PostgreSQL only.
    }
}
