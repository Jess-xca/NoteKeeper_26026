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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/workspaces")
@CrossOrigin(origins = "*")
public class WorkspaceController {

    @Autowired
    private WorkspaceService workspaceService;

    @Autowired
    private DTOMapper dtoMapper;

    @PostMapping
    public ResponseEntity<?> createWorkspace(@RequestBody Workspace workspace) {
        try {
            String result = workspaceService.createWorkspace(workspace);

            if (result.startsWith("error:")) {
                return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                Workspace createdWorkspace = workspaceService.getWorkspaceById(result);
                return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.toWorkspaceDTO(createdWorkspace));
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create workspace: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getWorkspaceById(@PathVariable String id) {
        try {
            Workspace workspace = workspaceService.getWorkspaceById(id);
            if (workspace == null) {
                return new ResponseEntity<>("Workspace not found", HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok(dtoMapper.toWorkspaceDTO(workspace));
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch workspace: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllWorkspaces() {
        try {
            List<Workspace> workspaces = workspaceService.getAllWorkspaces();
            List<WorkspaceDTO> workspaceDTOs = workspaces.stream()
                    .map(dtoMapper::toWorkspaceDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(workspaceDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch workspaces: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<?> getWorkspacesByOwner(@PathVariable String ownerId) {
        try {
            List<Workspace> workspaces = workspaceService.getWorkspacesByOwner(ownerId);
            List<WorkspaceDTO> workspaceDTOs = workspaces.stream()
                    .map(dtoMapper::toWorkspaceDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(workspaceDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch owner workspaces: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/inbox/{ownerId}")
    public ResponseEntity<?> getInboxWorkspace(@PathVariable String ownerId) {
        try {
            Workspace inbox = workspaceService.getInboxWorkspace(ownerId);
            if (inbox == null) {
                return new ResponseEntity<>("Inbox workspace not found", HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok(dtoMapper.toWorkspaceDTO(inbox));
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch inbox: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/empty/{ownerId}")
    public ResponseEntity<?> getEmptyWorkspaces(@PathVariable String ownerId) {
        try {
            List<Workspace> workspaces = workspaceService.getEmptyWorkspaces(ownerId);
            List<WorkspaceDTO> workspaceDTOs = workspaces.stream()
                    .map(dtoMapper::toWorkspaceDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(workspaceDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch empty workspaces: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchWorkspaces(@RequestParam String keyword) {
        try {
            List<Workspace> workspaces = workspaceService.searchWorkspaces(keyword);
            List<WorkspaceDTO> workspaceDTOs = workspaces.stream()
                    .map(dtoMapper::toWorkspaceDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(workspaceDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to search workspaces: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateWorkspace(
            @PathVariable String id,
            @RequestBody Workspace workspaceDetails) {
        try {
            String result = workspaceService.updateWorkspace(id, workspaceDetails);

            if (result.equals("not found")) {
                return new ResponseEntity<>("Workspace not found", HttpStatus.NOT_FOUND);
            } else {
                Workspace updatedWorkspace = workspaceService.getWorkspaceById(id);
                return ResponseEntity.ok(dtoMapper.toWorkspaceDTO(updatedWorkspace));
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update workspace: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWorkspace(@PathVariable String id) {
        try {
            String result = workspaceService.deleteWorkspace(id);

            if (result.equals("not found")) {
                return new ResponseEntity<>("Workspace not found", HttpStatus.NOT_FOUND);
            } else if (result.equals("cannot delete inbox")) {
                return new ResponseEntity<>("Cannot delete default Inbox workspace", HttpStatus.CONFLICT);
            } else {
                return new ResponseEntity<>("Workspace deleted successfully", HttpStatus.OK);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to delete workspace: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/owner/{ownerId}/sorted")
    public ResponseEntity<?> getWorkspacesSorted(
            @PathVariable String ownerId,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        try {
            List<Workspace> workspaces = workspaceService.getWorkspacesSorted(ownerId, sortBy, direction);
            List<WorkspaceDTO> workspaceDTOs = workspaces.stream()
                    .map(dtoMapper::toWorkspaceDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(workspaceDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to sort workspaces: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/paginated")
    public ResponseEntity<?> getWorkspacesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Workspace> workspaces = workspaceService.getWorkspacesPaginated(page, size);
            Page<WorkspaceDTO> workspaceDTOs = workspaces.map(dtoMapper::toWorkspaceDTO);
            return ResponseEntity.ok(workspaceDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch paginated workspaces: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/owner/{ownerId}/paginated")
    public ResponseEntity<?> getOwnerWorkspacesPaginated(
            @PathVariable String ownerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Workspace> workspaces = workspaceService.getOwnerWorkspacesPaginated(ownerId, page, size);
            Page<WorkspaceDTO> workspaceDTOs = workspaces.map(dtoMapper::toWorkspaceDTO);
            return ResponseEntity.ok(workspaceDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch owner workspaces: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/count")
    public ResponseEntity<?> countWorkspaces() {
        try {
            long count = workspaceService.countWorkspaces();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to count workspaces: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/count/owner/{ownerId}")
    public ResponseEntity<?> countOwnerWorkspaces(@PathVariable String ownerId) {
        try {
            long count = workspaceService.countOwnerWorkspaces(ownerId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to count owner workspaces: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{workspaceId}/count-pages")
    public ResponseEntity<?> countWorkspacePages(@PathVariable String workspaceId) {
        try {
            long count = workspaceService.countWorkspacePages(workspaceId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to count workspace pages: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
