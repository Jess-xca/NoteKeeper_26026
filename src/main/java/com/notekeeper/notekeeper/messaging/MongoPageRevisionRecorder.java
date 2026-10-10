package com.notekeeper.notekeeper.messaging;

import com.notekeeper.notekeeper.document.PageRevision;
import com.notekeeper.notekeeper.model.Page;
import com.notekeeper.notekeeper.repository.mongo.PageRevisionRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("docker")
public class MongoPageRevisionRecorder implements PageRevisionRecorder {

    private final PageRevisionRepository pageRevisionRepository;

    public MongoPageRevisionRecorder(PageRevisionRepository pageRevisionRepository) {
        this.pageRevisionRepository = pageRevisionRepository;
    }

    @Override
    public void record(Page page) {
        String workspaceId = page.getWorkspace() == null ? null : page.getWorkspace().getId();
        String authorId = page.getUser() == null ? null : page.getUser().getId();
        pageRevisionRepository.save(new PageRevision(
                page.getId(),
                page.getTitle(),
                page.getContent(),
                workspaceId,
                authorId));
    }
}
