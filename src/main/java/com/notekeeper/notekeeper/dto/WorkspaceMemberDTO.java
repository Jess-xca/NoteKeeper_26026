package com.notekeeper.notekeeper.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;

public class WorkspaceMemberDTO {
    private String id;

    @NotNull(message = "Workspace ID is required")
    private String workspaceId;

    @NotNull(message = "User ID is required")
    private String userId;

    private UserSummaryDTO user;

    private WorkspaceSummaryDTO workspace;

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "^(OWNER|EDITOR|VIEWER)$", message = "Role must be either OWNER, EDITOR, or VIEWER")
    private String role;

    private LocalDateTime joinedAt;

    public WorkspaceMemberDTO() {
    }

    public WorkspaceMemberDTO(String id, String workspaceId, String userId, UserSummaryDTO user,
            WorkspaceSummaryDTO workspace, String role, LocalDateTime joinedAt) {
        this.id = id;
        this.workspaceId = workspaceId;
        this.userId = userId;
        this.user = user;
        this.workspace = workspace;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
