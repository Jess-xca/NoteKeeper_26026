package com.notekeeper.notekeeper.controller;

import com.notekeeper.notekeeper.model.Workspace;
import com.notekeeper.notekeeper.service.WorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
@CrossOrigin(origins = "*")
public class WorkspaceController {

    @Autowired
    private WorkspaceService workspaceService;

    // CREATE
    @PostMapping
    public ResponseEntity<Workspace> createWorkspace(@RequestBody Workspace workspace) {
        try {
            Workspace createdWorkspace = workspaceService.createWorkspace(workspace);
            return new ResponseEntity<>(createdWorkspace, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // READ
    @GetMapping("/{id}")
    public ResponseEntity<Workspace> getWorkspaceById(@PathVariable String id) {
        try {
            Workspace workspace = workspaceService.getWorkspaceById(id);
            return new ResponseEntity<>(workspace, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<List<Workspace>> getAllWorkspaces() {
        try {
            List<Workspace> workspaces = workspaceService.getAllWorkspaces();
            return new ResponseEntity<>(workspaces, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<Workspace>> getWorkspacesByOwner(@PathVariable String ownerId) {
        try {
            List<Workspace> workspaces = workspaceService.getWorkspacesByOwner(ownerId);
            return new ResponseEntity<>(workspaces, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/inbox/{ownerId}")
    public ResponseEntity<Workspace> getInboxWorkspace(@PathVariable String ownerId) {
        try {
            Workspace inbox = workspaceService.getInboxWorkspace(ownerId);
            return new ResponseEntity<>(inbox, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/empty/{ownerId}")
    public ResponseEntity<List<Workspace>> getEmptyWorkspaces(@PathVariable String ownerId) {
        try {
            List<Workspace> workspaces = workspaceService.getEmptyWorkspaces(ownerId);
            return new ResponseEntity<>(workspaces, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Workspace>> searchWorkspaces(@RequestParam String keyword) {
        try {
            List<Workspace> workspaces = workspaceService.searchWorkspaces(keyword);
            return new ResponseEntity<>(workspaces, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Workspace> updateWorkspace(
            @PathVariable String id,
            @RequestBody Workspace workspaceDetails) {
        try {
            Workspace updatedWorkspace = workspaceService.updateWorkspace(id, workspaceDetails);
            return new ResponseEntity<>(updatedWorkspace, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteWorkspace(@PathVariable String id) {
        try {
            workspaceService.deleteWorkspace(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // SORTING & PAGINATION
    @GetMapping("/owner/{ownerId}/sorted")
    public ResponseEntity<List<Workspace>> getWorkspacesSorted(
            @PathVariable String ownerId,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        try {
            List<Workspace> workspaces = workspaceService.getWorkspacesSorted(ownerId, sortBy, direction);
            return new ResponseEntity<>(workspaces, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<Workspace>> getWorkspacesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Workspace> workspaces = workspaceService.getWorkspacesPaginated(page, size);
            return new ResponseEntity<>(workspaces, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/owner/{ownerId}/paginated")
    public ResponseEntity<Page<Workspace>> getOwnerWorkspacesPaginated(
            @PathVariable String ownerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Workspace> workspaces = workspaceService.getOwnerWorkspacesPaginated(ownerId, page, size);
            return new ResponseEntity<>(workspaces, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // STATISTICS
    @GetMapping("/count")
    public ResponseEntity<Long> countWorkspaces() {
        try {
            long count = workspaceService.countWorkspaces();
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/count/owner/{ownerId}")
    public ResponseEntity<Long> countOwnerWorkspaces(@PathVariable String ownerId) {
        try {
            long count = workspaceService.countOwnerWorkspaces(ownerId);
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{workspaceId}/count-pages")
    public ResponseEntity<Long> countWorkspacePages(@PathVariable String workspaceId) {
        try {
            long count = workspaceService.countWorkspacePages(workspaceId);
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}