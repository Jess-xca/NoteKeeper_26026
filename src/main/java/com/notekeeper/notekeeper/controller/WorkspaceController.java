package com.notekeeper.notekeeper.controller;

import com.notekeeper.notekeeper.dto.WorkspaceDTO;
import com.notekeeper.notekeeper.mapper.DTOMapper;
import com.notekeeper.notekeeper.model.Workspace;
import com.notekeeper.notekeeper.service.WorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/workspaces")
@CrossOrigin(origins = "*")
public class WorkspaceController {

    @Autowired
    private WorkspaceService workspaceService;

    @Autowired
    private DTOMapper dtoMapper;

    // CREATE
    @PostMapping
    public ResponseEntity<WorkspaceDTO> createWorkspace(@RequestBody Workspace workspace) {
        String resultId = workspaceService.createWorkspace(workspace);
        Workspace createdWorkspace = workspaceService.getWorkspaceById(resultId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.toWorkspaceDTO(createdWorkspace));
    }

    // READ
    @GetMapping("/{id}")
    public ResponseEntity<WorkspaceDTO> getWorkspaceById(@PathVariable String id) {
        Workspace workspace = workspaceService.getWorkspaceById(id);
        return ResponseEntity.ok(dtoMapper.toWorkspaceDTO(workspace));
    }

    @GetMapping
    public ResponseEntity<List<WorkspaceDTO>> getAllWorkspaces() {
        List<Workspace> workspaces = workspaceService.getAllWorkspaces();
        List<WorkspaceDTO> workspaceDTOs = workspaces.stream()
                .map(dtoMapper::toWorkspaceDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(workspaceDTOs);
    }

    @GetMapping("/my")
    public ResponseEntity<List<WorkspaceDTO>> getMyWorkspaces(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.notekeeper.notekeeper.security.UserPrincipal principal) {
        List<Workspace> workspaces = workspaceService.getWorkspacesByOwner(principal.getId());
        List<WorkspaceDTO> workspaceDTOs = workspaces.stream()
                .map(dtoMapper::toWorkspaceDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(workspaceDTOs);
    }

    @GetMapping("/my/shared")
    public ResponseEntity<List<WorkspaceDTO>> getMySharedWorkspaces(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.notekeeper.notekeeper.security.UserPrincipal principal) {
        List<Workspace> workspaces = workspaceService.getSharedWorkspaces(principal.getId());
        List<WorkspaceDTO> workspaceDTOs = workspaces.stream()
                .map(dtoMapper::toWorkspaceDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(workspaceDTOs);
    }

    @GetMapping("/my/inbox")
    public ResponseEntity<WorkspaceDTO> getMyInbox(@org.springframework.security.core.annotation.AuthenticationPrincipal com.notekeeper.notekeeper.security.UserPrincipal principal) {
        Workspace inbox = workspaceService.getInboxWorkspace(principal.getId());
        return ResponseEntity.ok(dtoMapper.toWorkspaceDTO(inbox));
    }

    @GetMapping("/empty/{ownerId}")
    public ResponseEntity<List<WorkspaceDTO>> getEmptyWorkspaces(@PathVariable String ownerId) {
        List<Workspace> workspaces = workspaceService.getEmptyWorkspaces(ownerId);
        List<WorkspaceDTO> workspaceDTOs = workspaces.stream()
                .map(dtoMapper::toWorkspaceDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(workspaceDTOs);
    }

    @GetMapping("/search")
    public ResponseEntity<List<WorkspaceDTO>> searchWorkspaces(@RequestParam String keyword) {
        List<Workspace> workspaces = workspaceService.searchWorkspaces(keyword);
        List<WorkspaceDTO> workspaceDTOs = workspaces.stream()
                .map(dtoMapper::toWorkspaceDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(workspaceDTOs);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkspaceDTO> updateWorkspace(
            @PathVariable String id,
            @RequestBody Workspace workspaceDetails) {
        workspaceService.updateWorkspace(id, workspaceDetails);
        Workspace updatedWorkspace = workspaceService.getWorkspaceById(id);
        return ResponseEntity.ok(dtoMapper.toWorkspaceDTO(updatedWorkspace));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteWorkspace(@PathVariable String id) {
        workspaceService.deleteWorkspace(id);
        return ResponseEntity.ok(Map.of("message", "Workspace deleted successfully"));
    }

    @GetMapping("/owner/{ownerId}/sorted")
    public ResponseEntity<List<WorkspaceDTO>> getWorkspacesSorted(
            @PathVariable String ownerId,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        List<Workspace> workspaces = workspaceService.getWorkspacesSorted(ownerId, sortBy, direction);
        List<WorkspaceDTO> workspaceDTOs = workspaces.stream()
                .map(dtoMapper::toWorkspaceDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(workspaceDTOs);
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<WorkspaceDTO>> getWorkspacesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Workspace> workspaces = workspaceService.getWorkspacesPaginated(page, size);
        Page<WorkspaceDTO> workspaceDTOs = workspaces.map(dtoMapper::toWorkspaceDTO);
        return ResponseEntity.ok(workspaceDTOs);
    }

    @GetMapping("/owner/{ownerId}/paginated")
    public ResponseEntity<Page<WorkspaceDTO>> getOwnerWorkspacesPaginated(
            @PathVariable String ownerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Workspace> workspaces = workspaceService.getOwnerWorkspacesPaginated(ownerId, page, size);
        Page<WorkspaceDTO> workspaceDTOs = workspaces.map(dtoMapper::toWorkspaceDTO);
        return ResponseEntity.ok(workspaceDTOs);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countWorkspaces() {
        return ResponseEntity.ok(workspaceService.countWorkspaces());
    }

    @GetMapping("/count/owner/{ownerId}")
    public ResponseEntity<Long> countOwnerWorkspaces(@PathVariable String ownerId) {
        return ResponseEntity.ok(workspaceService.countOwnerWorkspaces(ownerId));
    }

    @GetMapping("/{workspaceId}/count-pages")
    public ResponseEntity<Long> countWorkspacePages(@PathVariable String workspaceId) {
        return ResponseEntity.ok(workspaceService.countWorkspacePages(workspaceId));
    }
}
