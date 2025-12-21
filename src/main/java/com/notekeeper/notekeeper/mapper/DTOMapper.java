package com.notekeeper.notekeeper.mapper;

import com.notekeeper.notekeeper.dto.*;
import com.notekeeper.notekeeper.model.*;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class DTOMapper {

    // User Mappings
    public UserDTO toUserDTO(User user) {
        if (user == null)
            return null;

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setGender(user.getGender());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        if (user.getLocation() != null) {
            dto.setLocation(toLocationDTO(user.getLocation()));
        }

        if (user.getProfile() != null) {
            dto.setProfile(toUserProfileDTO(user.getProfile()));
        }

        return dto;
    }

    // Map UserDTO to User entity
    public User toUserEntity(UserDTO dto) {
        if (dto == null)
            return null;

        User user = new User();
        if (dto.getUsername() != null)
            user.setUsername(dto.getUsername());
        if (dto.getEmail() != null)
            user.setEmail(dto.getEmail());
        if (dto.getFirstName() != null)
            user.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null)
            user.setLastName(dto.getLastName());
        if (dto.getPhoneNumber() != null)
            user.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getDateOfBirth() != null)
            user.setDateOfBirth(dto.getDateOfBirth());
        if (dto.getGender() != null)
            user.setGender(dto.getGender());
        if (dto.getRole() != null)
            user.setRole(dto.getRole());
        if (dto.getPassword() != null)
            user.setPassword(dto.getPassword());

        return user;
    }

    public UserSummaryDTO toUserSummaryDTO(User user) {
        if (user == null)
            return null;

        return new UserSummaryDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName());
    }

    // Location Mappings
    public LocationDTO toLocationDTO(Location location) {
        if (location == null)
            return null;

        LocationDTO dto = new LocationDTO();
        dto.setId(location.getId());
        dto.setName(location.getName());
        dto.setCode(location.getCode());
        dto.setType(location.getType());
        dto.setCreatedAt(location.getCreatedAt());

        if (location.getParent() != null) {
            dto.setParent(toLocationDTO(location.getParent()));
        }

        return dto;
    }

    // UserProfile Mappings
    public UserProfileDTO toUserProfileDTO(UserProfile profile) {
        if (profile == null)
            return null;

        return new UserProfileDTO(
                profile.getId(),
                profile.getBio(),
                profile.getAvatarUrl(),
                profile.getTheme(),
                profile.getLanguage(),
                profile.getUpdatedAt());
    }

    // Workspace Mappings
    public WorkspaceDTO toWorkspaceDTO(Workspace workspace) {
        if (workspace == null)
            return null;

        return new WorkspaceDTO(
                workspace.getId(),
                workspace.getName(),
                workspace.getDescription(),
                workspace.getIcon(),
                toUserSummaryDTO(workspace.getOwner()),
                workspace.getIsDefault(),
                workspace.getCreatedAt(),
                workspace.getPages() != null ? workspace.getPages().size() : 0);
    }

    public WorkspaceSummaryDTO toWorkspaceSummaryDTO(Workspace workspace) {
        if (workspace == null)
            return null;

        return new WorkspaceSummaryDTO(
                workspace.getId(),
                workspace.getName(),
                workspace.getIcon());
    }

    // Page Mappings
    public PageDTO toPageDTO(Page page) {
        if (page == null)
            return null;

        PageDTO dto = new PageDTO();
        dto.setId(page.getId());
        dto.setTitle(page.getTitle());
        dto.setContent(page.getContent());
        dto.setIcon(page.getIcon());
        dto.setCoverImage(page.getCoverImage());
        dto.setIsFavorite(page.getIsFavorite());
        dto.setIsArchived(page.getIsArchived());
        dto.setUser(toUserSummaryDTO(page.getUser()));
        dto.setWorkspace(toWorkspaceSummaryDTO(page.getWorkspace()));
        dto.setCreatedAt(page.getCreatedAt());
        dto.setUpdatedAt(page.getUpdatedAt());

        // Map tags
        if (page.getPageTags() != null && !page.getPageTags().isEmpty()) {
            dto.setTags(page.getPageTags().stream()
                    .map(pt -> toTagDTO(pt.getTag()))
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    // Tag Mappings
    public TagDTO toTagDTO(Tag tag) {
        if (tag == null)
            return null;

        return new TagDTO(
                tag.getId(),
                tag.getName(),
                tag.getColor(),
                tag.getCreatedAt(),
                tag.getPageTags() != null ? tag.getPageTags().size() : 0);
    }

    // WorkspaceMember Mappings
    public WorkspaceMemberDTO toWorkspaceMemberDTO(WorkspaceMember member) {
        if (member == null)
            return null;

        WorkspaceMemberDTO dto = new WorkspaceMemberDTO();
        dto.setId(member.getId());
        dto.setWorkspaceId(member.getWorkspace().getId());
        dto.setUserId(member.getUser().getId());
        dto.setUser(toUserSummaryDTO(member.getUser()));
        dto.setWorkspace(toWorkspaceSummaryDTO(member.getWorkspace()));
        dto.setRole(member.getRole().name());
        dto.setJoinedAt(member.getJoinedAt());
        return dto;
    }

    // Notification Mappings
    public NotificationDTO toNotificationDTO(Notification notification) {
        if (notification == null)
            return null;

        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUser().getId());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setIsRead(notification.getIsRead());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setActionUrl(notification.getActionUrl());

        return dto;
    }
}
