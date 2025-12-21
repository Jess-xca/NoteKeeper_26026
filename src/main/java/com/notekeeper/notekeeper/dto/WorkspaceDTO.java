package com.notekeeper.notekeeper.dto;

import java.time.LocalDateTime;

public class WorkspaceDTO {
    private String id;
    private String name;
    private String description;
    private String icon;
    private UserSummaryDTO owner;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private Integer pageCount;

    public WorkspaceDTO() {
    }

    public WorkspaceDTO(String id, String name, String description, String icon,
            UserSummaryDTO owner, Boolean isDefault, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.owner = owner;
        this.isDefault = isDefault;
        this.createdAt = createdAt;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public UserSummaryDTO getOwner() {
        return owner;
    }

    public void setOwner(UserSummaryDTO owner) {
        this.owner = owner;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}