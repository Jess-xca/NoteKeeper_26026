package com.notekeeper.notekeeper.service;

import com.notekeeper.notekeeper.model.Page;
import com.notekeeper.notekeeper.model.User;
import com.notekeeper.notekeeper.model.Workspace;
import com.notekeeper.notekeeper.repository.PageRepository;
import com.notekeeper.notekeeper.repository.UserRepository;
import com.notekeeper.notekeeper.repository.WorkspaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PageService {

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    // CREATE
    public Page createPage(Page page) {
        return pageRepository.save(page);
    }

    public Page createQuickNote(String userId, String title, String content) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Workspace inbox = workspaceRepository.findByOwnerIdAndIsDefaultTrue(userId)
                .orElseThrow(() -> new RuntimeException("Inbox not found"));

        Page quickNote = new Page(title, content, user, inbox);
        return pageRepository.save(quickNote);
    }

    // READ
    public Page getPageById(String id) {
        return pageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Page not found with id: " + id));
    }

    public List<Page> getAllPages() {
        return pageRepository.findAll();
    }

    public List<Page> getPagesByUser(String userId) {
        return pageRepository.findByUserId(userId);
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
    public Page updatePage(String id, Page pageDetails) {
        Page page = getPageById(id);
        page.setTitle(pageDetails.getTitle());
        page.setContent(pageDetails.getContent());
        page.setIcon(pageDetails.getIcon());
        page.setCoverImage(pageDetails.getCoverImage());
        return pageRepository.save(page);
    }

    public Page toggleFavorite(String pageId) {
        Page page = getPageById(pageId);
        page.setIsFavorite(!page.getIsFavorite());
        return pageRepository.save(page);
    }

    public Page toggleArchive(String pageId) {
        Page page = getPageById(pageId);
        page.setIsArchived(!page.getIsArchived());
        return pageRepository.save(page);
    }

    public Page movePage(String pageId, String workspaceId) {
        Page page = getPageById(pageId);
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace not found"));
        page.setWorkspace(workspace);
        return pageRepository.save(page);
    }

    // DELETE
    public void deletePage(String id) {
        Page page = getPageById(id);
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
        return pageRepository.findByUserId(userId, PageRequest.of(page, size));
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