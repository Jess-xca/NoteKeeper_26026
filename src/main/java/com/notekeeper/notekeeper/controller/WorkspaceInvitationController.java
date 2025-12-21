package com.notekeeper.notekeeper.controller;

import com.notekeeper.notekeeper.dto.NotificationDTO;
import com.notekeeper.notekeeper.mapper.DTOMapper;
import com.notekeeper.notekeeper.model.Notification;
import com.notekeeper.notekeeper.model.NotificationType;
import com.notekeeper.notekeeper.model.User;
import com.notekeeper.notekeeper.model.Workspace;
import com.notekeeper.notekeeper.repository.NotificationRepository;
import com.notekeeper.notekeeper.repository.UserRepository;
import com.notekeeper.notekeeper.repository.WorkspaceRepository;
import com.notekeeper.notekeeper.service.PermissionService;
import com.notekeeper.notekeeper.service.WorkspaceMemberService;
import com.notekeeper.notekeeper.exception.ResourceNotFoundException;
import com.notekeeper.notekeeper.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/invitations")
@CrossOrigin(origins = "*")
public class WorkspaceInvitationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private WorkspaceMemberService workspaceMemberService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private DTOMapper dtoMapper;

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Send a workspace invitation to a user by email
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendInvitation(
            @org.springframework.security.core.annotation.AuthenticationPrincipal 
            com.notekeeper.notekeeper.security.UserPrincipal principal,
            @RequestParam String workspaceId,
            @RequestParam String email,
            @RequestParam(defaultValue = "VIEWER") String role) {

        // Validate role
        if (!role.equals("VIEWER") && !role.equals("EDITOR")) {
            throw new BadRequestException("Role must be VIEWER or EDITOR");
        }

        // Check if user has permission to invite (must be workspace owner)
        permissionService.validateWorkspaceAccess(workspaceId, principal.getId(), 
            com.notekeeper.notekeeper.model.WorkspaceRole.OWNER);

        // Find the workspace
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        // Find the invited user by email
        User invitedUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // Check if user is already a member
        if (workspaceMemberService.isUserMember(workspaceId, invitedUser.getId())) {
            throw new BadRequestException("User is already a member of this workspace");
        }

        // Find the inviter
        User inviter = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Inviter not found"));

        // Create metadata JSON
        Map<String, String> metadataMap = new HashMap<>();
        metadataMap.put("workspaceId", workspaceId);
        metadataMap.put("workspaceName", workspace.getName());
        metadataMap.put("role", role);
        metadataMap.put("inviterId", principal.getId());
        metadataMap.put("inviterName", inviter.getFullName());

        String metadata;
        try {
            metadata = objectMapper.writeValueAsString(metadataMap);
        } catch (Exception e) {
            metadata = "{}";
        }

        // Create the invitation notification
        Notification invitation = new Notification();
        invitation.setUser(invitedUser);
        invitation.setTitle("Workspace Invitation");
        invitation.setMessage(inviter.getFullName() + " invited you to join \"" + workspace.getName() + "\" as " + role);
        invitation.setType(NotificationType.WORKSPACE_INVITE);
        invitation.setMetadata(metadata);
        invitation.setStatus("PENDING");
        invitation.setIsRead(false);

        notificationRepository.save(invitation);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Invitation sent successfully"));
    }

    /**
     * Accept a workspace invitation
     */
    @PostMapping("/{notificationId}/accept")
    public ResponseEntity<Map<String, String>> acceptInvitation(
            @org.springframework.security.core.annotation.AuthenticationPrincipal 
            com.notekeeper.notekeeper.security.UserPrincipal principal,
            @PathVariable String notificationId) {

        Notification invitation = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        // Verify this invitation belongs to the current user
        if (!invitation.getUser().getId().equals(principal.getId())) {
            throw new BadRequestException("This invitation is not for you");
        }

        // Verify it's a workspace invite
        if (invitation.getType() != NotificationType.WORKSPACE_INVITE) {
            throw new BadRequestException("This is not a workspace invitation");
        }

        // Check if already processed
        if (!"PENDING".equals(invitation.getStatus())) {
            throw new BadRequestException("This invitation has already been " + invitation.getStatus().toLowerCase());
        }

        // Parse metadata
        String workspaceId;
        String role;
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> metadataMap = objectMapper.readValue(invitation.getMetadata(), Map.class);
            workspaceId = metadataMap.get("workspaceId");
            role = metadataMap.get("role");
        } catch (Exception e) {
            throw new BadRequestException("Invalid invitation data");
        }

        // Add user to workspace
        workspaceMemberService.addMember(workspaceId, principal.getId(), role);

        // Update invitation status
        invitation.setStatus("ACCEPTED");
        invitation.setIsRead(true);
        notificationRepository.save(invitation);

        return ResponseEntity.ok(Map.of("message", "Invitation accepted! You are now a member of the workspace."));
    }

    /**
     * Decline a workspace invitation
     */
    @PostMapping("/{notificationId}/decline")
    public ResponseEntity<Map<String, String>> declineInvitation(
            @org.springframework.security.core.annotation.AuthenticationPrincipal 
            com.notekeeper.notekeeper.security.UserPrincipal principal,
            @PathVariable String notificationId) {

        Notification invitation = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        // Verify this invitation belongs to the current user
        if (!invitation.getUser().getId().equals(principal.getId())) {
            throw new BadRequestException("This invitation is not for you");
        }

        // Verify it's a workspace invite
        if (invitation.getType() != NotificationType.WORKSPACE_INVITE) {
            throw new BadRequestException("This is not a workspace invitation");
        }

        // Check if already processed
        if (!"PENDING".equals(invitation.getStatus())) {
            throw new BadRequestException("This invitation has already been " + invitation.getStatus().toLowerCase());
        }

        // Update invitation status
        invitation.setStatus("DECLINED");
        invitation.setIsRead(true);
        notificationRepository.save(invitation);

        return ResponseEntity.ok(Map.of("message", "Invitation declined."));
    }
}
