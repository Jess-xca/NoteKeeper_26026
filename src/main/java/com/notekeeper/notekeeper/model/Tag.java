package com.notekeeper.notekeeper.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tags")
public class Tag {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false, length = 7)
    private String color = "#3B82F6";

    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PageTag> pageTags = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Tag() {
    }

    public Tag(String name, String color) {
        this.name = name;
        this.color = color;
    }

    @PrePersist
    protected void onCreate() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public List<PageTag> getPageTags() {
        return pageTags;
    }

    public void setPageTags(List<PageTag> pageTags) {
        this.pageTags = pageTags;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
