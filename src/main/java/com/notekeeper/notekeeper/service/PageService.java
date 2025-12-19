package com.notekeeper.notekeeper.service;

import com.notekeeper.notekeeper.model.Page;
import com.notekeeper.notekeeper.model.User;
import com.notekeeper.notekeeper.model.Workspace;
import com.notekeeper.notekeeper.repository.PageRepository;
import com.notekeeper.notekeeper.repository.TagRepository;
import com.notekeeper.notekeeper.repository.PageTagRepository;
import com.notekeeper.notekeeper.repository.UserRepository;
import com.notekeeper.notekeeper.repository.WorkspaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    @Autowired
    private PageTagRepository pageTagRepository;

    // CREATE
    public String createPage(Page page) {
        try {
            Page saved = pageRepository.save(page);
            return saved.getId();
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    public String createQuickNote(String userId, String title, String content) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return "user not found";
        }

        Optional<Workspace> inboxOpt = workspaceRepository.findByOwnerIdAndIsDefaultTrue(userId);
        if (!inboxOpt.isPresent()) {
            return "inbox not found";
        }

        Page quickNote = new Page(title, content, userOpt.get(), inboxOpt.get());
        Page saved = pageRepository.save(quickNote);
        return saved.getId();
    }

    // READ
    public Page getPageById(String id) {
        Optional<Page> page = pageRepository.findById(id);
        return page.orElse(null);
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
    public String updatePage(String id, Page pageDetails) {
        Optional<Page> pageOpt = pageRepository.findById(id);

        if (!pageOpt.isPresent()) {
            return "not found";
        }

        Page page = pageOpt.get();
        page.setTitle(pageDetails.getTitle());
        page.setContent(pageDetails.getContent());
        page.setIcon(pageDetails.getIcon());
        page.setCoverImage(pageDetails.getCoverImage());

        // Sync tags if they are provided in pageDetails
        if (pageDetails.getPageTags() != null) {
            // Remove existing tags that are not in the new list
            page.getPageTags().removeIf(existingPt -> 
                pageDetails.getPageTags().stream()
                    .noneMatch(newPt -> newPt.getTag().getId().equals(existingPt.getTag().getId()))
            );

            // Add new tags that are not already present
            for (com.notekeeper.notekeeper.model.PageTag newPt : pageDetails.getPageTags()) {
                boolean exists = page.getPageTags().stream()
                    .anyMatch(existingPt -> existingPt.getTag().getId().equals(newPt.getTag().getId()));
                
                if (!exists) {
                    Optional<com.notekeeper.notekeeper.model.Tag> tagOpt = tagRepository.findById(newPt.getTag().getId());
                    if (tagOpt.isPresent()) {
                        com.notekeeper.notekeeper.model.PageTag pt = new com.notekeeper.notekeeper.model.PageTag(page, tagOpt.get());
                        page.getPageTags().add(pt);
                    }
                }
            }
        }

        pageRepository.save(page);
        return "success";
    }

    public String toggleFavorite(String pageId) {
        Optional<Page> pageOpt = pageRepository.findById(pageId);

        if (!pageOpt.isPresent()) {
            return "not found";
        }

        Page page = pageOpt.get();
        page.setIsFavorite(!page.getIsFavorite());
        pageRepository.save(page);
        return "success";
    }

    public String toggleArchive(String pageId) {
        Optional<Page> pageOpt = pageRepository.findById(pageId);

        if (!pageOpt.isPresent()) {
            return "not found";
        }

        Page page = pageOpt.get();
        page.setIsArchived(!page.getIsArchived());
        pageRepository.save(page);
        return "success";
    }

    public String movePage(String pageId, String workspaceId) {
        Optional<Page> pageOpt = pageRepository.findById(pageId);
        if (!pageOpt.isPresent()) {
            return "page not found";
        }

        Optional<Workspace> workspaceOpt = workspaceRepository.findById(workspaceId);
        if (!workspaceOpt.isPresent()) {
            return "workspace not found";
        }

        Page page = pageOpt.get();
        page.setWorkspace(workspaceOpt.get());
        pageRepository.save(page);
        return "success";
    }

    // DELETE
    public String deletePage(String id) {
        Optional<Page> pageOpt = pageRepository.findById(id);

        if (!pageOpt.isPresent()) {
            return "not found";
        }

        pageRepository.delete(pageOpt.get());
        return "success";
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