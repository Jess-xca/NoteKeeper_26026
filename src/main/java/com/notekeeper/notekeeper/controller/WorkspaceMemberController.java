package com.notekeeper.notekeeper.controller;

import com.notekeeper.notekeeper.dto.WorkspaceMemberDTO;
import com.notekeeper.notekeeper.service.WorkspaceMemberService;
import com.notekeeper.notekeeper.model.WorkspaceRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/members")
@CrossOrigin(origins = "*")
public class WorkspaceMemberController {

    @Autowired
    private WorkspaceMemberService workspaceMemberService;

    @PostMapping
    public ResponseEntity<?> addMember(
            @PathVariable String workspaceId,
            @RequestParam String userId,
            @RequestParam(defaultValue = "VIEWER") String role) {
        try {
            if (!WorkspaceRole.isValid(role)) {
                return new ResponseEntity<>("Invalid role. Must be OWNER, EDITOR, or VIEWER", HttpStatus.BAD_REQUEST);
            }

            String result = workspaceMemberService.addMember(workspaceId, userId, role);

            switch (result) {
                case "workspace not found":
                    return new ResponseEntity<>("Workspace not found", HttpStatus.NOT_FOUND);
                case "user not found":
                    return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
                case "user already member":
                    return new ResponseEntity<>("User is already a member of this workspace", HttpStatus.CONFLICT);
                case "invalid role":
                    return new ResponseEntity<>("Invalid role. Must be OWNER, EDITOR, or VIEWER",
                            HttpStatus.BAD_REQUEST);
                default:
                    return new ResponseEntity<>("Member added successfully", HttpStatus.CREATED);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to add member: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> removeMember(
            @PathVariable String workspaceId,
            @PathVariable String userId) {
        try {
            String result = workspaceMemberService.removeMember(workspaceId, userId);

            if (result.equals("member not found")) {
                return new ResponseEntity<>("Member not found", HttpStatus.NOT_FOUND);
            } else if (result.equals("cannot remove owner")) {
                return new ResponseEntity<>("Cannot remove workspace owner", HttpStatus.FORBIDDEN);
            } else {
                return new ResponseEntity<>("Member removed successfully", HttpStatus.OK);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to remove member: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<?> updateMemberRole(
            @PathVariable String workspaceId,
            @PathVariable String userId,
            @RequestParam String role) {
        try {
            if (!WorkspaceRole.isValid(role)) {
                return new ResponseEntity<>("Invalid role. Must be OWNER, EDITOR, or VIEWER", HttpStatus.BAD_REQUEST);
            }

            String result = workspaceMemberService.updateMemberRole(workspaceId, userId, role);

            switch (result) {
                case "member not found":
                    return new ResponseEntity<>("Member not found", HttpStatus.NOT_FOUND);
                case "cannot change owner role":
                    return new ResponseEntity<>("Cannot change workspace owner role", HttpStatus.FORBIDDEN);
                case "invalid role":
                    return new ResponseEntity<>("Invalid role. Must be OWNER, EDITOR, or VIEWER",
                            HttpStatus.BAD_REQUEST);
                default:
                    return new ResponseEntity<>("Member role updated successfully", HttpStatus.OK);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update member role: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<?> getWorkspaceMembers(@PathVariable String workspaceId) {
        try {
            List<WorkspaceMemberDTO> members = workspaceMemberService.getWorkspaceMembers(workspaceId);
            return ResponseEntity.ok(members);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch workspace members: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/check/{userId}")
    public ResponseEntity<?> checkMembership(
            @PathVariable String workspaceId,
            @PathVariable String userId) {
        try {
            WorkspaceMemberDTO member = workspaceMemberService.getMember(workspaceId, userId);
            if (member == null) {
                return ResponseEntity.ok(null);
            }
            return ResponseEntity.ok(member);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to check membership: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
