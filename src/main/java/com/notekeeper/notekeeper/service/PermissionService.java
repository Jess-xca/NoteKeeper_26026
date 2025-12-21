package com.notekeeper.notekeeper.service;

import com.notekeeper.notekeeper.exception.UnauthorizedException;
import com.notekeeper.notekeeper.model.Page;
import com.notekeeper.notekeeper.model.PageShare;
import com.notekeeper.notekeeper.model.WorkspaceMember;
import com.notekeeper.notekeeper.model.WorkspaceRole;
import com.notekeeper.notekeeper.repository.PageRepository;
import com.notekeeper.notekeeper.repository.PageShareRepository;
import com.notekeeper.notekeeper.repository.WorkspaceMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private PageShareRepository pageShareRepository;

    @Autowired
    private WorkspaceMemberRepository workspaceMemberRepository;

    /**
     * Validates if a user has permission to access or modify a page.
     * 
     * @param pageId             The page ID
     * @param userId             The user ID
     * @param requiredPermission "READ" or "EDIT"
     */
    public void validatePageAccess(String pageId, String userId, String requiredPermission) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new com.notekeeper.notekeeper.exception.ResourceNotFoundException("Page not found"));

        // 1. Owner has full access
        if (page.getUser().getId().equals(userId)) {
            return;
        }

        // 2. Check direct page shares
        PageShare share = pageShareRepository.findByPageIdAndSharedWithId(pageId, userId).orElse(null);
        if (share != null) {
            if ("READ".equals(requiredPermission))
                return;
            if ("EDIT".equals(requiredPermission) && "EDIT".equals(share.getPermission()))
                return;
        }

        // 3. Check workspace membership
        if (page.getWorkspace() != null) {
            WorkspaceMember member = workspaceMemberRepository
                    .findByWorkspaceIdAndUserId(page.getWorkspace().getId(), userId).orElse(null);
            if (member != null) {
                if ("READ".equals(requiredPermission))
                    return;
                if ("EDIT".equals(requiredPermission)
                        && (member.getRole() == WorkspaceRole.OWNER || member.getRole() == WorkspaceRole.EDITOR))
                    return;
            }
        }

        throw new UnauthorizedException(
                "You do not have permission to " + requiredPermission.toLowerCase() + " this page");
    }

    /**
     * Validates if a user can manage a workspace.
     */
    public void validateWorkspaceAccess(String workspaceId, String userId, WorkspaceRole requiredRole) {
        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this workspace"));

        if (getRoleLevel(member.getRole()) < getRoleLevel(requiredRole)) {
            throw new UnauthorizedException("Insufficient workspace permissions");
        }
    }

    private int getRoleLevel(WorkspaceRole role) {
        switch (role) {
            case OWNER:
                return 3;
            case EDITOR:
                return 2;
            case VIEWER:
                return 1;
            default:
                return 0;
        }
    }
}
