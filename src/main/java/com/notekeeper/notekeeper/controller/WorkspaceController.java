// package com.notekeeper.notekeeper.controller;

// import com.notekeeper.notekeeper.model.Workspace;
// import com.notekeeper.notekeeper.service.WorkspaceService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.data.domain.Page;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;

// @RestController
// @RequestMapping("/api/workspaces")
// public class WorkspaceController {

// @Autowired
// private WorkspaceService workspaceService;

// // CREATE
// @PostMapping
// public ResponseEntity<Workspace> createWorkspace(@RequestBody Workspace
// workspace) {
// Workspace createdWorkspace = workspaceService.createWorkspace(workspace);
// return ResponseEntity.status(HttpStatus.CREATED).body(createdWorkspace);
// }

// // READ - All workspaces
// @GetMapping
// public ResponseEntity<List<Workspace>> getAllWorkspaces() {
// List<Workspace> workspaces = workspaceService.getAllWorkspaces();
// return ResponseEntity.ok(workspaces);
// }

// // READ - By ID
// @GetMapping("/{id}")
// public ResponseEntity<Workspace> getWorkspaceById(@PathVariable String id) {
// Workspace workspace = workspaceService.getWorkspaceById(id);
// return ResponseEntity.ok(workspace);
// }

// // READ - By owner
// @GetMapping("/owner/{ownerId}")
// public ResponseEntity<List<Workspace>> getWorkspacesByOwner(@PathVariable
// String ownerId) {
// List<Workspace> workspaces = workspaceService.getWorkspacesByOwner(ownerId);
// return ResponseEntity.ok(workspaces);
// }

// // READ - Inbox
// @GetMapping("/owner/{ownerId}/inbox")
// public ResponseEntity<Workspace> getInboxWorkspace(@PathVariable String
// ownerId) {
// Workspace workspace = workspaceService.getInboxWorkspace(ownerId);
// return ResponseEntity.ok(workspace);
// }

// // READ - Empty workspaces
// @GetMapping("/owner/{ownerId}/empty")
// public ResponseEntity<List<Workspace>> getEmptyWorkspaces(@PathVariable
// String ownerId) {
// List<Workspace> workspaces = workspaceService.getEmptyWorkspaces(ownerId);
// return ResponseEntity.ok(workspaces);
// }

// // SEARCH
// @GetMapping("/search")
// public ResponseEntity<List<Workspace>> searchWorkspaces(@RequestParam String
// keyword) {
// List<Workspace> workspaces = workspaceService.searchWorkspaces(keyword);
// return ResponseEntity.ok(workspaces);
// }

// // UPDATE
// @PutMapping("/{id}")
// public ResponseEntity<Workspace> updateWorkspace(@PathVariable String id,
// @RequestBody Workspace workspace) {
// Workspace updatedWorkspace = workspaceService.updateWorkspace(id, workspace);
// return ResponseEntity.ok(updatedWorkspace);
// }

// // DELETE
// @DeleteMapping("/{id}")
// public ResponseEntity<Void> deleteWorkspace(@PathVariable String id) {
// workspaceService.deleteWorkspace(id);
// return ResponseEntity.noContent().build();
// }

// // SORTING
// @GetMapping("/owner/{ownerId}/sorted")
// public ResponseEntity<List<Workspace>> getWorkspacesSorted(
// @PathVariable String ownerId,
// @RequestParam(defaultValue = "createdAt") String sortBy,
// @RequestParam(defaultValue = "desc") String direction) {
// List<Workspace> workspaces = workspaceService.getWorkspacesSorted(ownerId,
// sortBy, direction);
// return ResponseEntity.ok(workspaces);
// }

// // PAGINATION
// @GetMapping("/paginated")
// public ResponseEntity<Page<Workspace>> getWorkspacesPaginated(
// @RequestParam(defaultValue = "0") int page,
// @RequestParam(defaultValue = "10") int size) {
// Page<Workspace> workspaces = workspaceService.getWorkspacesPaginated(page,
// size);
// return ResponseEntity.ok(workspaces);
// }

// @GetMapping("/owner/{ownerId}/paginated")
// public ResponseEntity<Page<Workspace>> getOwnerWorkspacesPaginated(
// @PathVariable String ownerId,
// @RequestParam(defaultValue = "0") int page,
// @RequestParam(defaultValue = "10") int size) {
// Page<Workspace> workspaces =
// workspaceService.getOwnerWorkspacesPaginated(ownerId, page, size);
// return ResponseEntity.ok(workspaces);
// }

// // STATISTICS
// @GetMapping("/count")
// public ResponseEntity<Long> countWorkspaces() {
// long count = workspaceService.countWorkspaces();
// return ResponseEntity.ok(count);
// }

// @GetMapping("/count/owner/{ownerId}")
// public ResponseEntity<Long> countOwnerWorkspaces(@PathVariable String
// ownerId) {
// long count = workspaceService.countOwnerWorkspaces(ownerId);
// return ResponseEntity.ok(count);
// }

// @GetMapping("/{workspaceId}/count-pages")
// public ResponseEntity<Long> countWorkspacePages(@PathVariable String
// workspaceId) {
// long count = workspaceService.countWorkspacePages(workspaceId);
// return ResponseEntity.ok(count);
// }
// }