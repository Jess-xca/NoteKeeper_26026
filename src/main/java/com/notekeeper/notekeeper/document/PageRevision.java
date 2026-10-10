package com.notekeeper.notekeeper.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "page_revisions")
public class PageRevision {

    @Id
    private String id;

    @Indexed
    private String pageId;

    private String title;
    private String content;
    private String workspaceId;
    private String authorId;
    private LocalDateTime savedAt;

    public PageRevision() {
    }

    public PageRevision(String pageId, String title, String content, String workspaceId, String authorId) {
        this.pageId = pageId;
        this.title = title;
        this.content = content;
        this.workspaceId = workspaceId;
        this.authorId = authorId;
        this.savedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getPageId() {
        return pageId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public LocalDateTime getSavedAt() {
        return savedAt;
    }
}
