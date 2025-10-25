package com.notekeeper.notekeeper.model;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;

@Entity
@Table(name = "page_tags")
public class PageTag {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    // Many-to-One: Many PageTags link to one Page
    @ManyToOne
    @JoinColumn(name = "page_id", nullable = false)
    private Page page;

    // Many-to-One: Many PageTags link to one Tag
    @ManyToOne
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @Column(nullable = false)
    private LocalDateTime taggedAt;

    // Constructor
    public PageTag() {
        this.taggedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public void setTaggedAt(LocalDateTime taggedAt) {
        this.taggedAt = taggedAt;
    }
}