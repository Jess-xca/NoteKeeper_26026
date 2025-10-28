package com.notekeeper.notekeeper.dto;

import java.time.LocalDateTime;

public class UserProfileDTO {
    private String id;
    private String bio;
    private String avatarUrl;
    private String theme;
    private String language;
    private LocalDateTime updatedAt;

    public UserProfileDTO() {
    }

    public UserProfileDTO(String id, String bio, String avatarUrl, String theme,
            String language, LocalDateTime updatedAt) {
        this.id = id;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.theme = theme;
        this.language = language;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}