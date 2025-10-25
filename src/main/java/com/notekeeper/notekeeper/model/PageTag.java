package com.notekeeper.notekeeper.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "page_tags")
public class PageTag {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = false)
    private Page page;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @Column(nullable = false, updatable = false)
    private LocalDateTime taggedAt;

    public PageTag() {
    }

    public PageTag(Page page, Tag tag) {
        this.page = page;
        this.tag = tag;
    }

    @PrePersist
    protected void onCreate() {
        this.id = UUID.randomUUID().toString();
        this.taggedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public LocalDateTime getTaggedAt() {
        return taggedAt;
    }
}