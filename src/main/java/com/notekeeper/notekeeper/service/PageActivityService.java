package com.notekeeper.notekeeper.service;

import com.notekeeper.notekeeper.dto.PageListItemDTO;
import com.notekeeper.notekeeper.mapper.DTOMapper;
import com.notekeeper.notekeeper.model.Page;
import com.notekeeper.notekeeper.model.PageView;
import com.notekeeper.notekeeper.model.WorkspaceMember;
import com.notekeeper.notekeeper.model.WorkspaceRole;
import com.notekeeper.notekeeper.repository.PageRepository;
import com.notekeeper.notekeeper.repository.PageViewRepository;
import com.notekeeper.notekeeper.repository.WorkspaceMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * "Shared with me" and "Recent history" features.
 */
@Service
public class PageActivityService {

    static final int RECENT_LIMIT = 20;
    private static final int PREVIEW_LENGTH = 160;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private PageViewRepository pageViewRepository;

    @Autowired
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private DTOMapper dtoMapper;

    /**
     * Pages from workspaces owned by other people that the user was invited to.
     * The user's own workspaces and pages never appear here.
     */
    @Transactional(readOnly = true)
    public List<PageListItemDTO> getSharedWithMe(String userId) {
        Map<String, WorkspaceRole> roles = workspaceMemberRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(m -> m.getWorkspace().getId(), WorkspaceMember::getRole, (a, b) -> a));

        return pageRepository.findSharedWithUser(userId).stream()
                .map(page -> toItem(page, roles.get(page.getWorkspace().getId()), userId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countSharedWithMe(String userId) {
        return pageRepository.findSharedWithUser(userId).size();
    }

    /**
     * Remember that the user opened the page. Never throws: a history failure
     * must not stop somebody from reading their note.
     */
    public void recordView(String userId, String pageId) {
        if (userId == null || pageId == null) {
            return;
        }
        try {
            PageView view = pageViewRepository.findByUserIdAndPageId(userId, pageId).orElse(null);
            if (view == null) {
                pageViewRepository.save(new PageView(userId, pageId));
            } else {
                view.touch();
                pageViewRepository.save(view);
            }
        } catch (Exception e) {
            // Two tabs opening the same page at once can hit the unique constraint; that is harmless.
            System.out.println("Could not record page view: " + e.getMessage());
        }
    }

    /**
     * Last pages the user opened, newest first. Pages that were deleted, or that
     * the user can no longer read, are left out (and cleaned up if deleted).
     */
    @Transactional
    public List<PageListItemDTO> getRecentHistory(String userId) {
        List<PageView> views = pageViewRepository.findByUserIdOrderByLastViewedAtDesc(userId,
                PageRequest.of(0, RECENT_LIMIT * 2));
        if (views.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, Page> pagesById = pageRepository
                .findAllById(views.stream().map(PageView::getPageId).collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(Page::getId, Function.identity()));

        List<PageListItemDTO> result = new ArrayList<>();
        Map<String, WorkspaceRole> roleCache = new HashMap<>();
        for (PageView view : views) {
            if (result.size() >= RECENT_LIMIT) {
                break;
            }
            Page page = pagesById.get(view.getPageId());
            if (page == null) {
                pageViewRepository.delete(view);
                continue;
            }
            try {
                permissionService.validatePageAccess(page.getId(), userId, "READ");
            } catch (Exception e) {
                continue; // access was removed since the page was opened
            }
            WorkspaceRole role = roleCache.computeIfAbsent(page.getWorkspace().getId(),
                    id -> permissionService.getWorkspaceRole(id, userId));
            PageListItemDTO item = toItem(page, role, userId);
            item.setLastViewedAt(view.getLastViewedAt());
            result.add(item);
        }
        return result;
    }

    public void clearHistory(String userId) {
        pageViewRepository.deleteByUserId(userId);
    }

    private PageListItemDTO toItem(Page page, WorkspaceRole role, String userId) {
        PageListItemDTO dto = new PageListItemDTO();
        dto.setId(page.getId());
        dto.setTitle(page.getTitle());
        dto.setPreview(preview(page.getContent()));
        dto.setIcon(page.getIcon());
        dto.setWorkspace(dtoMapper.toWorkspaceSummaryDTO(page.getWorkspace()));
        dto.setOwner(dtoMapper.toUserSummaryDTO(page.getWorkspace().getOwner()));
        dto.setAuthor(dtoMapper.toUserSummaryDTO(page.getUser()));
        dto.setMyRole(role != null ? role.name() : "VIEWER");
        dto.setShared(!page.getWorkspace().getOwner().getId().equals(userId));
        dto.setUpdatedAt(page.getUpdatedAt());
        return dto;
    }

    static String preview(String content) {
        if (content == null) {
            return "";
        }
        String text = content.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
        return text.length() > PREVIEW_LENGTH ? text.substring(0, PREVIEW_LENGTH) + "..." : text;
    }
}
