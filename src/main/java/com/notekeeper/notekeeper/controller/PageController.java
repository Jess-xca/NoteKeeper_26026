package com.notekeeper.notekeeper.controller;

import com.notekeeper.notekeeper.dto.PageDTO;
import com.notekeeper.notekeeper.mapper.DTOMapper;
import com.notekeeper.notekeeper.model.Page;
import com.notekeeper.notekeeper.service.PageService;
import com.notekeeper.notekeeper.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pages")
@CrossOrigin(origins = "*")
public class PageController {

    @Autowired
    private PageService pageService;
    
    @Autowired
    private com.notekeeper.notekeeper.repository.PageRepository pageRepository;

    @Autowired
    private DTOMapper dtoMapper;

    @Autowired
    private com.notekeeper.notekeeper.service.PermissionService permissionService;

    // CREATE
    @PostMapping
    public ResponseEntity<PageDTO> createPage(@RequestBody Page page) {
        String resultId = pageService.createPage(page);
        Page createdPage = pageService.getPageById(resultId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.toPageDTO(createdPage));
    }

    @PostMapping("/quick-note")
    public ResponseEntity<PageDTO> createQuickNote(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.notekeeper.notekeeper.security.UserPrincipal principal,
            @RequestParam String title,
            @RequestParam String content) {
        String resultId = pageService.createQuickNote(principal.getId(), title, content);
        Page quickNote = pageService.getPageById(resultId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.toPageDTO(quickNote));
    }

    @GetMapping("/my")
    public ResponseEntity<List<PageDTO>> getMyPages(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.notekeeper.notekeeper.security.UserPrincipal principal) {
        List<Page> pages = pageService.getPagesByUser(principal.getId());
        List<PageDTO> pageDTOs = pages.stream()
                .map(dtoMapper::toPageDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(pageDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PageDTO> getPageById(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.notekeeper.notekeeper.security.UserPrincipal principal,
            @PathVariable String id) {
        permissionService.validatePageAccess(id, principal.getId(), "READ");
        Page page = pageService.getPageById(id);
        return ResponseEntity.ok(dtoMapper.toPageDTO(page));
    }

    @GetMapping
    public ResponseEntity<List<PageDTO>> getAllPages() {
        List<Page> pages = pageService.getAllPages();
        List<PageDTO> pageDTOs = pages.stream()
                .map(dtoMapper::toPageDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(pageDTOs);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PageDTO>> getPagesByUser(@PathVariable String userId) {
        List<Page> pages = pageService.getPagesByUser(userId);
        List<PageDTO> pageDTOs = pages.stream()
                .map(dtoMapper::toPageDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(pageDTOs);
    }

    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<List<PageDTO>> getPagesByWorkspace(@PathVariable String workspaceId) {
        List<Page> pages = pageService.getPagesByWorkspace(workspaceId);
        List<PageDTO> pageDTOs = pages.stream()
                .map(dtoMapper::toPageDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(pageDTOs);
    }

    @GetMapping("/favorites/{userId}")
    public ResponseEntity<List<PageDTO>> getFavoritePages(@PathVariable String userId) {
        List<Page> pages = pageService.getFavoritePages(userId);
        List<PageDTO> pageDTOs = pages.stream()
                .map(dtoMapper::toPageDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(pageDTOs);
    }

    @GetMapping("/archived/{userId}")
    public ResponseEntity<List<PageDTO>> getArchivedPages(@PathVariable String userId) {
        List<Page> pages = pageService.getArchivedPages(userId);
        List<PageDTO> pageDTOs = pages.stream()
                .map(dtoMapper::toPageDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(pageDTOs);
    }

    @GetMapping("/inbox/{userId}")
    public ResponseEntity<List<PageDTO>> getInboxPages(@PathVariable String userId) {
        List<Page> pages = pageService.getInboxPages(userId);
        List<PageDTO> pageDTOs = pages.stream()
                .map(dtoMapper::toPageDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(pageDTOs);
    }

    @GetMapping("/recent/{userId}")
    public ResponseEntity<List<PageDTO>> getRecentPages(@PathVariable String userId) {
        List<Page> pages = pageService.getRecentPages(userId);
        List<PageDTO> pageDTOs = pages.stream()
                .map(dtoMapper::toPageDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(pageDTOs);
    }

    @GetMapping("/search")
    public ResponseEntity<List<PageDTO>> searchPages(@RequestParam String keyword) {
        List<Page> pages = pageService.searchPages(keyword);
        List<PageDTO> pageDTOs = pages.stream()
                .map(dtoMapper::toPageDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(pageDTOs);
    }

    @GetMapping("/search/user/{userId}")
    public ResponseEntity<List<PageDTO>> searchUserPages(
            @PathVariable String userId,
            @RequestParam String keyword) {
        List<Page> pages = pageService.searchUserPages(userId, keyword);
        List<PageDTO> pageDTOs = pages.stream()
                .map(dtoMapper::toPageDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(pageDTOs);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PageDTO> updatePage(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.notekeeper.notekeeper.security.UserPrincipal principal,
            @PathVariable String id,
            @RequestBody Page pageDetails) {
        permissionService.validatePageAccess(id, principal.getId(), "EDIT");
        pageService.updatePage(id, pageDetails);
        Page updatedPage = pageService.getPageById(id);
        return ResponseEntity.ok(dtoMapper.toPageDTO(updatedPage));
    }

    @PutMapping("/{id}/toggle-favorite")
    public ResponseEntity<PageDTO> toggleFavorite(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.notekeeper.notekeeper.security.UserPrincipal principal,
            @PathVariable String id) {
        permissionService.validatePageAccess(id, principal.getId(), "READ");
        pageService.toggleFavorite(id);
        Page page = pageService.getPageById(id);
        return ResponseEntity.ok(dtoMapper.toPageDTO(page));
    }

    @PutMapping("/{id}/toggle-archive")
    public ResponseEntity<PageDTO> toggleArchive(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.notekeeper.notekeeper.security.UserPrincipal principal,
            @PathVariable String id) {
        permissionService.validatePageAccess(id, principal.getId(), "EDIT");
        pageService.toggleArchive(id);
        Page page = pageService.getPageById(id);
        return ResponseEntity.ok(dtoMapper.toPageDTO(page));
    }

    @PutMapping("/{id}/move")
    public ResponseEntity<PageDTO> movePage(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.notekeeper.notekeeper.security.UserPrincipal principal,
            @PathVariable String id,
            @RequestParam String workspaceId) {
        permissionService.validatePageAccess(id, principal.getId(), "EDIT");
        permissionService.validateWorkspaceAccess(workspaceId, principal.getId(), com.notekeeper.notekeeper.model.WorkspaceRole.EDITOR);
        pageService.movePage(id, workspaceId);
        Page page = pageService.getPageById(id);
        return ResponseEntity.ok(dtoMapper.toPageDTO(page));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deletePage(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.notekeeper.notekeeper.security.UserPrincipal principal,
            @PathVariable String id) {
        // Only owner can delete for now, or use "owner" check in validatePageAccess if we want to allow editors to delete?
        // Let's assume only owner or workspace OWNER can delete.
        Page page = pageService.getPageById(id);
        if (!page.getUser().getId().equals(principal.getId())) {
             // Check if it's in a workspace and user is OWNER of that workspace
             if (page.getWorkspace() != null) {
                 permissionService.validateWorkspaceAccess(page.getWorkspace().getId(), principal.getId(), com.notekeeper.notekeeper.model.WorkspaceRole.OWNER);
             } else {
                 throw new UnauthorizedException("Only the owner can delete this page");
             }
        }
        pageService.deletePage(id);
        return ResponseEntity.ok(Map.of("message", "Page deleted successfully"));
    }

    @GetMapping("/user/{userId}/sorted")
    public ResponseEntity<?> getUserPagesSorted(
            @PathVariable String userId,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        try {
            List<Page> pages = pageService.getUserPagesSorted(userId, sortBy, direction);
            List<PageDTO> pageDTOs = pages.stream()
                    .map(dtoMapper::toPageDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(pageDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to sort pages: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/paginated")
    public ResponseEntity<?> getPagesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            org.springframework.data.domain.Page<Page> pages = pageService.getPagesPaginated(page, size);
            org.springframework.data.domain.Page<PageDTO> pageDTOs = pages.map(dtoMapper::toPageDTO);
            return ResponseEntity.ok(pageDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch paginated pages: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/{userId}/paginated")
    public ResponseEntity<?> getUserPagesPaginated(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            org.springframework.data.domain.Page<Page> pages = pageService.getUserPagesPaginated(userId, page, size);
            org.springframework.data.domain.Page<PageDTO> pageDTOs = pages.map(dtoMapper::toPageDTO);
            return ResponseEntity.ok(pageDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch user pages: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/count/user/{userId}")
    public ResponseEntity<?> countUserPages(@PathVariable String userId) {
        try {
            long count = pageService.countUserPages(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to count pages: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/count/workspace/{workspaceId}")
    public ResponseEntity<?> countWorkspacePages(@PathVariable String workspaceId) {
        try {
            long count = pageService.countWorkspacePages(workspaceId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to count pages: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/count/favorites/{userId}")
    public ResponseEntity<?> countFavoritePages(@PathVariable String userId) {
        try {
            long count = pageService.countFavoritePages(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to count favorite pages: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get weekly statistics
    @GetMapping("/weekly-stats/{userId}")
    public ResponseEntity<Map<String, Object>> getWeeklyStats(@PathVariable String userId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfThisWeek = now.with(DayOfWeek.MONDAY).truncatedTo(ChronoUnit.DAYS);
            LocalDateTime startOfLastWeek = startOfThisWeek.minusWeeks(1);
            
            long thisWeekCount = pageRepository.countByUserIdAndCreatedAtBetween(userId, startOfThisWeek, now);
            long lastWeekCount = pageRepository.countByUserIdAndCreatedAtBetween(userId, startOfLastWeek, startOfThisWeek);
            
            double percentageChange = 0;
            if (lastWeekCount > 0) {
                percentageChange = ((double)(thisWeekCount - lastWeekCount) / lastWeekCount) * 100;
            }
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("thisWeek", thisWeekCount);
            stats.put("lastWeek", lastWeekCount);
            stats.put("percentageChange", Math.round(percentageChange));
            stats.put("isIncrease", thisWeekCount >= lastWeekCount);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get daily activity for chart (last 7 days)
    @GetMapping("/activity/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getActivity(@PathVariable String userId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Map<String, Object>> activityData = new java.util.ArrayList<>();
            
            for (int i = 6; i >= 0; i--) {
                LocalDateTime startOfDay = now.minusDays(i).truncatedTo(ChronoUnit.DAYS);
                LocalDateTime endOfDay = startOfDay.plusDays(1);
                long count = pageRepository.countByUserIdAndCreatedAtBetween(userId, startOfDay, endOfDay);
                
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", startOfDay.toLocalDate().toString());
                dayData.put("count", count);
                activityData.add(dayData);
            }
            
            return ResponseEntity.ok(activityData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
