package com.notekeeper.notekeeper.controller;

import com.notekeeper.notekeeper.dto.WorkspaceMemberDTO;
import com.notekeeper.notekeeper.service.WorkspaceMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/members")
@CrossOrigin(origins = "*")
public class WorkspaceMemberController {

    @Autowired
    private WorkspaceMemberService workspaceMemberService;

    @PostMapping
    public ResponseEntity<Map<String, String>> addMember(
            @PathVariable String workspaceId,
            @RequestParam String userId,
            @RequestParam(defaultValue = "VIEWER") String role) {
        workspaceMemberService.addMember(workspaceId, userId, role);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Member added successfully"));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, String>> removeMember(
            @PathVariable String workspaceId,
            @PathVariable String userId) {
        workspaceMemberService.removeMember(workspaceId, userId);
        return ResponseEntity.ok(Map.of("message", "Member removed successfully"));
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<Map<String, String>> updateMemberRole(
            @PathVariable String workspaceId,
            @PathVariable String userId,
            @RequestParam String role) {
        workspaceMemberService.updateMemberRole(workspaceId, userId, role);
        return ResponseEntity.ok(Map.of("message", "Member role updated successfully"));
    }

    @GetMapping
    public ResponseEntity<List<WorkspaceMemberDTO>> getWorkspaceMembers(@PathVariable String workspaceId) {
        List<WorkspaceMemberDTO> members = workspaceMemberService.getWorkspaceMembers(workspaceId);
        return ResponseEntity.ok(members);
    }

    @GetMapping("/check/{userId}")
    public ResponseEntity<WorkspaceMemberDTO> checkMembership(
            @PathVariable String workspaceId,
            @PathVariable String userId) {
        WorkspaceMemberDTO member = workspaceMemberService.getMember(workspaceId, userId);
        return ResponseEntity.ok(member);
    }
}
