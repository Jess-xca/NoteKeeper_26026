package com.notekeeper.notekeeper.service;

import com.notekeeper.notekeeper.model.Page;
import com.notekeeper.notekeeper.model.User;
import com.notekeeper.notekeeper.model.Workspace;
import com.notekeeper.notekeeper.repository.PageRepository;
import com.notekeeper.notekeeper.repository.TagRepository;
import com.notekeeper.notekeeper.repository.UserRepository;
import com.notekeeper.notekeeper.repository.WorkspaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.notekeeper.notekeeper.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class PageService {

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private TagRepository tagRepository;

    // CREATE
    @Transactional
    public String createPage(Page page) {
        // Handle tags during creation if present
        if (page.getPageTags() != null && !page.getPageTags().isEmpty()) {
            List<com.notekeeper.notekeeper.model.PageTag> incomingTags = new java.util.ArrayList<>(page.getPageTags());
            page.getPageTags().clear();
            for (com.notekeeper.notekeeper.model.PageTag pt : incomingTags) {
                com.notekeeper.notekeeper.model.Tag tag = tagRepository.findById(pt.getTag().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Tag not found: " + pt.getTag().getId()));
                page.getPageTags().add(new com.notekeeper.notekeeper.model.PageTag(page, tag));
            }
        }
        Page saved = pageRepository.save(page);
        return saved.getId();
    }

    @Transactional
    public String createQuickNote(String userId, String title, String content) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Workspace inbox = workspaceRepository.findByOwnerIdAndIsDefaultTrue(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Inbox workspace not found"));

        Page quickNote = new Page(title, content, user, inbox);
        Page saved = pageRepository.save(quickNote);
        return saved.getId();
    }

    // READ
    public Page getPageById(String id) {
        return pageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with id: " + id));
    }

    public List<Page> getAllPages() {
        return pageRepository.findAll();
    }

    public List<Page> getPagesByUser(String userId) {
        return pageRepository.findByUserIdAndIsArchivedFalse(userId);
    }

    public List<Page> getPagesByWorkspace(String workspaceId) {
        return pageRepository.findByWorkspaceId(workspaceId);
    }

    public List<Page> getFavoritePages(String userId) {
        return pageRepository.findByUserIdAndIsFavoriteTrue(userId);
    }

    public List<Page> getArchivedPages(String userId) {
        return pageRepository.findByUserIdAndIsArchivedTrue(userId);
    }

    public List<Page> getInboxPages(String userId) {
        return pageRepository.findInboxPages(userId);
    }

    public List<Page> searchPages(String keyword) {
        return pageRepository.searchPages(keyword);
    }

    public List<Page> searchUserPages(String userId, String keyword) {
        return pageRepository.searchUserPages(userId, keyword);
    }

    // UPDATE
    @Transactional
    public void updatePage(String id, Page pageDetails) {
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found"));

        page.setTitle(pageDetails.getTitle());
        page.setContent(pageDetails.getContent());
        page.setIcon(pageDetails.getIcon());
        page.setCoverImage(pageDetails.getCoverImage());

        // Sync tags if they are provided in pageDetails
        if (pageDetails.getPageTags() != null) {
            syncTags(page, pageDetails.getPageTags());
        }

        pageRepository.save(page);
    }

    private void syncTags(Page page, List<com.notekeeper.notekeeper.model.PageTag> newPageTags) {
        // Remove existing tags that are not in the new list
        page.getPageTags().removeIf(existingPt -> newPageTags.stream()
                .noneMatch(newPt -> newPt.getTag().getId().equals(existingPt.getTag().getId())));

        // Add new tags that are not already present
        for (com.notekeeper.notekeeper.model.PageTag newPt : newPageTags) {
            boolean exists = page.getPageTags().stream()
                    .anyMatch(existingPt -> existingPt.getTag().getId().equals(newPt.getTag().getId()));

            if (!exists) {
                com.notekeeper.notekeeper.model.Tag tag = tagRepository.findById(newPt.getTag().getId())
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Tag not found: " + newPt.getTag().getId()));
                com.notekeeper.notekeeper.model.PageTag pt = new com.notekeeper.notekeeper.model.PageTag(page, tag);
                page.getPageTags().add(pt);
            }
        }
    }

    @Transactional
    public void toggleFavorite(String pageId) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found"));

        page.setIsFavorite(!page.getIsFavorite());
        pageRepository.save(page);
    }

    @Transactional
    public void toggleArchive(String pageId) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found"));

        page.setIsArchived(!page.getIsArchived());
        pageRepository.save(page);
    }

    @Transactional
    public void movePage(String pageId, String workspaceId) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found"));

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        page.setWorkspace(workspace);
        pageRepository.save(page);
    }

    // DELETE
    @Transactional
    public void deletePage(String id) {
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found"));

        pageRepository.delete(page);
    }

    // SORTING
    public List<Page> getUserPagesSorted(String userId, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return pageRepository.findByUserId(userId, sort);
    }

    public List<Page> getRecentPages(String userId) {
        return pageRepository.findTop10ByUserIdOrderByUpdatedAtDesc(userId);
    }

    // PAGINATION
    public org.springframework.data.domain.Page<Page> getPagesPaginated(int page, int size) {
        return pageRepository.findAll(PageRequest.of(page, size));
    }

    public org.springframework.data.domain.Page<Page> getUserPagesPaginated(String userId, int page, int size) {
        return pageRepository.findByUserIdAndIsArchivedFalse(userId, PageRequest.of(page, size));
    }

    // STATISTICS
    public long countUserPages(String userId) {
        return pageRepository.countByUserId(userId);
    }

    public long countWorkspacePages(String workspaceId) {
        return pageRepository.countByWorkspaceId(workspaceId);
    }

    public long countFavoritePages(String userId) {
        return pageRepository.countByUserIdAndIsFavoriteTrue(userId);
    }
}