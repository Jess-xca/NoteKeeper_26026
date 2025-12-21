package com.notekeeper.notekeeper.service;

import com.notekeeper.notekeeper.dto.WorkspaceMemberDTO;
import com.notekeeper.notekeeper.mapper.DTOMapper;
import com.notekeeper.notekeeper.model.User;
import com.notekeeper.notekeeper.model.Workspace;
import com.notekeeper.notekeeper.model.WorkspaceMember;
import com.notekeeper.notekeeper.model.WorkspaceRole;
import com.notekeeper.notekeeper.repository.UserRepository;
import com.notekeeper.notekeeper.repository.WorkspaceMemberRepository;
import com.notekeeper.notekeeper.repository.WorkspaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.notekeeper.notekeeper.exception.ResourceNotFoundException;
import com.notekeeper.notekeeper.exception.BadRequestException;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WorkspaceMemberService {

    @Autowired
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DTOMapper dtoMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailService emailService;

    @Transactional
    public void addMember(String workspaceId, String userId, String role) {
        if (!WorkspaceRole.isValid(role)) {
            throw new BadRequestException("Invalid role. Must be OWNER, EDITOR, or VIEWER");
        }

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        User user = userRepository.findById(userId)
                .orElseGet(() -> userRepository.findByEmail(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with ID or Email: " + userId)));

        if (workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw new BadRequestException("User is already a member of this workspace");
        }

        WorkspaceMember member = new WorkspaceMember(workspace, user,
                WorkspaceRole.valueOf(role.toUpperCase()));
        workspaceMemberRepository.save(member);

        // CREATE NOTIFICATION
        notificationService.createNotification(
                userId,
                "Added to Workspace",
                "You have been added to the workspace: " + workspace.getName(),
                com.notekeeper.notekeeper.model.NotificationType.SHARE
        );

        // SEND EMAIL
        emailService.sendShareNotification(
                user.getEmail(),
                workspace.getOwner().getFullName(),
                workspace.getName(),
                "Workspace"
        );
    }

    @Transactional
    public void removeMember(String workspaceId, String userId) {
        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        if (member.getRole() == WorkspaceRole.OWNER) {
            throw new BadRequestException("Cannot remove workspace owner");
        }

        workspaceMemberRepository.delete(member);
    }

    @Transactional
    public void updateMemberRole(String workspaceId, String userId, String newRole) {
        if (!WorkspaceRole.isValid(newRole)) {
            throw new BadRequestException("Invalid role. Must be OWNER, EDITOR, or VIEWER");
        }

        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        if (member.getRole() == WorkspaceRole.OWNER && !newRole.equalsIgnoreCase("OWNER")) {
            throw new BadRequestException("Cannot change workspace owner role");
        }

        member.setRole(WorkspaceRole.valueOf(newRole.toUpperCase()));
        workspaceMemberRepository.save(member);
    }

    public List<WorkspaceMemberDTO> getWorkspaceMembers(String workspaceId) {
        List<WorkspaceMember> members = workspaceMemberRepository.findByWorkspaceId(workspaceId);
        return members.stream()
                .map(dtoMapper::toWorkspaceMemberDTO)
                .collect(Collectors.toList());
    }

    public List<WorkspaceMemberDTO> getUserWorkspaces(String userId) {
        List<WorkspaceMember> members = workspaceMemberRepository.findByUserId(userId);
        return members.stream()
                .map(dtoMapper::toWorkspaceMemberDTO)
                .collect(Collectors.toList());
    }

    public boolean isUserMember(String workspaceId, String userId) {
        return workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId);
    }

    public WorkspaceMemberDTO getMember(String workspaceId, String userId) {
        return workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .map(dtoMapper::toWorkspaceMemberDTO)
                .orElse(null);
    }

    public boolean hasPermission(String workspaceId, String userId, String requiredRole) {
        Optional<WorkspaceMember> memberOpt = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId);
        if (!memberOpt.isPresent()) {
            return false;
        }

        WorkspaceRole userRole = memberOpt.get().getRole();
        return getRoleLevel(userRole) >= getRoleLevel(WorkspaceRole.valueOf(requiredRole.toUpperCase()));
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
