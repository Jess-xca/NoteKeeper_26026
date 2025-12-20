package com.notekeeper.notekeeper.service;

import com.notekeeper.notekeeper.model.Workspace;
import com.notekeeper.notekeeper.model.WorkspaceMember;
import com.notekeeper.notekeeper.repository.WorkspaceRepository;
import com.notekeeper.notekeeper.repository.WorkspaceMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.notekeeper.notekeeper.exception.ResourceNotFoundException;
import com.notekeeper.notekeeper.exception.BadRequestException;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class WorkspaceService {

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private WorkspaceMemberRepository workspaceMemberRepository;

    // CREATE
    @Transactional
    public String createWorkspace(Workspace workspace) {
        Workspace saved = workspaceRepository.save(workspace);
        return saved.getId();
    }

    // READ
    public Workspace getWorkspaceById(String id) {
        return workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
    }

    public List<Workspace> getAllWorkspaces() {
        return workspaceRepository.findAll();
    }

    public List<Workspace> getWorkspacesByOwner(String ownerId) {
        return workspaceRepository.findByOwnerId(ownerId);
    }

    public List<Workspace> getSharedWorkspaces(String userId) {
        return workspaceMemberRepository.findByUserId(userId).stream()
                .filter(member -> !member.getWorkspace().getOwner().getId().equals(userId))
                .map(WorkspaceMember::getWorkspace)
                .toList();
    }

    public Workspace getInboxWorkspace(String ownerId) {
        return workspaceRepository.findByOwnerIdAndIsDefaultTrue(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Inbox workspace not found"));
    }

    public List<Workspace> searchWorkspaces(String keyword) {
        return workspaceRepository.searchWorkspaces(keyword);
    }

    public List<Workspace> getEmptyWorkspaces(String ownerId) {
        return workspaceRepository.findEmptyWorkspaces(ownerId);
    }

    // UPDATE
    @Transactional
    public void updateWorkspace(String id, Workspace workspaceDetails) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        workspace.setName(workspaceDetails.getName());
        workspace.setDescription(workspaceDetails.getDescription());
        workspace.setIcon(workspaceDetails.getIcon());
        workspaceRepository.save(workspace);
    }

    // DELETE
    @Transactional
    public void deleteWorkspace(String id) {
        Workspace workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        if (workspace.getIsDefault()) {
            throw new BadRequestException("Cannot delete inbox");
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
