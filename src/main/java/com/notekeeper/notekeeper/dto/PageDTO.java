package com.notekeeper.notekeeper.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PageDTO {
    private String id;
    private String title;
    private String content;
    private String icon;
    private String coverImage;
    private Boolean isFavorite;
    private Boolean isArchived;
    private UserSummaryDTO user;
    private WorkspaceSummaryDTO workspace;
    private List<TagDTO> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PageDTO() {
    }

    public PageDTO(String id, String title, String content, String icon, String coverImage,
            Boolean isFavorite, Boolean isArchived, UserSummaryDTO user,
            WorkspaceSummaryDTO workspace, List<TagDTO> tags,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.icon = icon;
        this.coverImage = coverImage;
        this.isFavorite = isFavorite;
        this.isArchived = isArchived;
        this.user = user;
        this.workspace = workspace;
        this.tags = tags;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public Boolean getIsFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(Boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public Boolean getIsArchived() {
        return isArchived;
    }

    public void setIsArchived(Boolean isArchived) {
        this.isArchived = isArchived;
    }

    public UserSummaryDTO getUser() {
        return user;
    }

    public void setUser(UserSummaryDTO user) {
        this.user = user;
    }

    public WorkspaceSummaryDTO getWorkspace() {
        return workspace;
    }

    public void setWorkspace(WorkspaceSummaryDTO workspace) {
        this.workspace = workspace;
    }

    public List<TagDTO> getTags() {
        return tags;
    }

    public void setTags(List<TagDTO> tags) {
        this.tags = tags;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}