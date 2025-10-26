package com.notekeeper.notekeeper.service;

import com.notekeeper.notekeeper.model.Workspace;
import com.notekeeper.notekeeper.repository.WorkspaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkspaceService {

    @Autowired
    private WorkspaceRepository workspaceRepository;

    // CREATE
    public Workspace createWorkspace(Workspace workspace) {
        return workspaceRepository.save(workspace);
    }

    // READ
    public Workspace getWorkspaceById(String id) {
        return workspaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workspace not found with id: " + id));
    }

    public List<Workspace> getAllWorkspaces() {
        return workspaceRepository.findAll();
    }

    public List<Workspace> getWorkspacesByOwner(String ownerId) {
        return workspaceRepository.findByOwnerId(ownerId);
    }

    public Workspace getInboxWorkspace(String ownerId) {
        return workspaceRepository.findByOwnerIdAndIsDefaultTrue(ownerId)
                .orElseThrow(() -> new RuntimeException("Inbox not found"));
    }

    public List<Workspace> searchWorkspaces(String keyword) {
        return workspaceRepository.searchWorkspaces(keyword);
    }

    public List<Workspace> getEmptyWorkspaces(String ownerId) {
        return workspaceRepository.findEmptyWorkspaces(ownerId);
    }

    // UPDATE
    public Workspace updateWorkspace(String id, Workspace workspaceDetails) {
        Workspace workspace = getWorkspaceById(id);
        workspace.setName(workspaceDetails.getName());
        workspace.setDescription(workspaceDetails.getDescription());
        workspace.setIcon(workspaceDetails.getIcon());
        return workspaceRepository.save(workspace);
    }

    // DELETE
    public void deleteWorkspace(String id) {
        Workspace workspace = getWorkspaceById(id);
        if (workspace.getIsDefault()) {
            throw new RuntimeException("Cannot delete default Inbox workspace");
        }
        workspaceRepository.delete(workspace);
    }

    // SORTING
    public List<Workspace> getWorkspacesSorted(String ownerId, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return workspaceRepository.findByOwnerId(ownerId, sort);
    }

    // PAGINATION
    public Page<Workspace> getWorkspacesPaginated(int page, int size) {
        return workspaceRepository.findAll(PageRequest.of(page, size));
    }

    public Page<Workspace> getOwnerWorkspacesPaginated(String ownerId, int page, int size) {
        return workspaceRepository.findByOwnerId(ownerId, PageRequest.of(page, size));
    }

    // STATISTICS
    public long countWorkspaces() {
        return workspaceRepository.count();
    }

    public long countOwnerWorkspaces(String ownerId) {
        return workspaceRepository.countByOwnerId(ownerId);
    }

    public long countWorkspacePages(String workspaceId) {
        return workspaceRepository.countPagesByWorkspaceId(workspaceId);
    }
}
