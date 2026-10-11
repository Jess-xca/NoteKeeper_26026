package com.notekeeper.notekeeper.dto;

import java.time.LocalDateTime;

/**
 * Lightweight page row used by "Shared with me" and "Recent history".
 */
public class PageListItemDTO {
    private String id;
    private String title;
    private String preview;
    private String icon;
    private WorkspaceSummaryDTO workspace;
    /** Owner of the workspace the page lives in. */
    private UserSummaryDTO owner;
    /** Person who wrote the page. */
    private UserSummaryDTO author;
    /** The caller's role in the workspace: OWNER, EDITOR or VIEWER. */
    private String myRole;
    /** True when the page is in a workspace owned by somebody else. */
    private boolean shared;
    private LocalDateTime updatedAt;
    private LocalDateTime lastViewedAt;

    public PageListItemDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public WorkspaceSummaryDTO getWorkspace() {
        return workspace;
    }

    public void setWorkspace(WorkspaceSummaryDTO workspace) {
        this.workspace = workspace;
    }

    public UserSummaryDTO getOwner() {
        return owner;
    }

    public void setOwner(UserSummaryDTO owner) {
        this.owner = owner;
    }

    public UserSummaryDTO getAuthor() {
        return author;
    }

    public void setAuthor(UserSummaryDTO author) {
        this.author = author;
    }

    public String getMyRole() {
        return myRole;
    }

    public void setMyRole(String myRole) {
        this.myRole = myRole;
    }

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getLastViewedAt() {
        return lastViewedAt;
    }

    public void setLastViewedAt(LocalDateTime lastViewedAt) {
        this.lastViewedAt = lastViewedAt;
    }
}
