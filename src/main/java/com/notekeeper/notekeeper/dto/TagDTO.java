package com.notekeeper.notekeeper.dto;

import java.time.LocalDateTime;

public class TagDTO {
    private String id;
    private String name;
    private String color;
    private LocalDateTime createdAt;
    private Integer pageCount;

    public TagDTO() {
    }

    public TagDTO(String id, String name, String color, LocalDateTime createdAt, Integer pageCount) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.createdAt = createdAt;
        this.pageCount = pageCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }
}