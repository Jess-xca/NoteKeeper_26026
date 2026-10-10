package com.notekeeper.notekeeper.messaging;

import com.notekeeper.notekeeper.model.Page;

public interface PageRevisionRecorder {

    void record(Page page);
}
