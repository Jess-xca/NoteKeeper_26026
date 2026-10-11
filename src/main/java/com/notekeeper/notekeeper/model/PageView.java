package com.notekeeper.notekeeper.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Remembers when a user last opened a page. One row per (user, page),
 * so the table stays small and the Recent history list needs no de-duplication.
 */
@Entity
@Table(name = "page_views", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "page_id" }), indexes = @Index(name = "idx_page_views_user_time", columnList = "user_id, last_viewed_at"))
public class PageView {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "page_id", nullable = false)
    private String pageId;

    @Column(name = "last_viewed_at", nullable = false)
    private LocalDateTime lastViewedAt;

    public PageView() {
    }

    public PageView(String userId, String pageId) {
        this.userId = userId;
        this.pageId = pageId;
        this.lastViewedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.lastViewedAt == null) {
            this.lastViewedAt = LocalDateTime.now();
        }
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getPageId() {
        return pageId;
    }

    public LocalDateTime getLastViewedAt() {
        return lastViewedAt;
    }

    public void touch() {
        this.lastViewedAt = LocalDateTime.now();
    }
}
