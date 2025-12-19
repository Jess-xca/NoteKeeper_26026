package com.notekeeper.notekeeper.controller;

import com.notekeeper.notekeeper.dto.PageDTO;
import com.notekeeper.notekeeper.mapper.DTOMapper;
import com.notekeeper.notekeeper.model.Page;
import com.notekeeper.notekeeper.service.PageService;
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

    @PostMapping
    public ResponseEntity<?> createPage(@RequestBody Page page) {
        try {
            String result = pageService.createPage(page);
            if (result.startsWith("error:")) {
                return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Page createdPage = pageService.getPageById(result);
            return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.toPageDTO(createdPage));
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create page: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/quick-note")
    public ResponseEntity<?> createQuickNote(
            @RequestParam String userId,
            @RequestParam String title,
            @RequestParam String content) {
        try {
            String result = pageService.createQuickNote(userId, title, content);

            if (result.equals("user not found")) {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            } else if (result.equals("inbox not found")) {
                return new ResponseEntity<>("Inbox workspace not found", HttpStatus.NOT_FOUND);
            } else if (result.startsWith("error:")) {
                return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                Page quickNote = pageService.getPageById(result);
                return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.toPageDTO(quickNote));
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create quick note: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPageById(@PathVariable String id) {
        try {
            Page page = pageService.getPageById(id);
            if (page == null) {
                return new ResponseEntity<>("Page not found", HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok(dtoMapper.toPageDTO(page));
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch page: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllPages() {
        try {
            List<Page> pages = pageService.getAllPages();
            List<PageDTO> pageDTOs = pages.stream()
                    .map(dtoMapper::toPageDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(pageDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch pages: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getPagesByUser(@PathVariable String userId) {
        try {
            List<Page> pages = pageService.getPagesByUser(userId);
            List<PageDTO> pageDTOs = pages.stream()
                    .map(dtoMapper::toPageDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(pageDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch user pages: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<?> getPagesByWorkspace(@PathVariable String workspaceId) {
        try {
            List<Page> pages = pageService.getPagesByWorkspace(workspaceId);
            List<PageDTO> pageDTOs = pages.stream()
                    .map(dtoMapper::toPageDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(pageDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch workspace pages: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/favorites/{userId}")
    public ResponseEntity<?> getFavoritePages(@PathVariable String userId) {
        try {
            List<Page> pages = pageService.getFavoritePages(userId);
            List<PageDTO> pageDTOs = pages.stream()
                    .map(dtoMapper::toPageDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(pageDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch favorite pages: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/archived/{userId}")
    public ResponseEntity<?> getArchivedPages(@PathVariable String userId) {
        try {
            List<Page> pages = pageService.getArchivedPages(userId);
            List<PageDTO> pageDTOs = pages.stream()
                    .map(dtoMapper::toPageDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(pageDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch archived pages: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/inbox/{userId}")
    public ResponseEntity<?> getInboxPages(@PathVariable String userId) {
        try {
            List<Page> pages = pageService.getInboxPages(userId);
            List<PageDTO> pageDTOs = pages.stream()
                    .map(dtoMapper::toPageDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(pageDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch inbox pages: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/recent/{userId}")
    public ResponseEntity<?> getRecentPages(@PathVariable String userId) {
        try {
            List<Page> pages = pageService.getRecentPages(userId);
            List<PageDTO> pageDTOs = pages.stream()
                    .map(dtoMapper::toPageDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(pageDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch recent pages: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchPages(@RequestParam String keyword) {
        try {
            List<Page> pages = pageService.searchPages(keyword);
            List<PageDTO> pageDTOs = pages.stream()
                    .map(dtoMapper::toPageDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(pageDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to search pages: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/search/user/{userId}")
    public ResponseEntity<?> searchUserPages(
            @PathVariable String userId,
            @RequestParam String keyword) {
        try {
            List<Page> pages = pageService.searchUserPages(userId, keyword);
            List<PageDTO> pageDTOs = pages.stream()
                    .map(dtoMapper::toPageDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(pageDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to search user pages: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePage(
            @PathVariable String id,
            @RequestBody Page pageDetails) {
        try {
            String result = pageService.updatePage(id, pageDetails);

            if (result.equals("not found")) {
                return new ResponseEntity<>("Page not found", HttpStatus.NOT_FOUND);
            } else {
                Page updatedPage = pageService.getPageById(id);
                return ResponseEntity.ok(dtoMapper.toPageDTO(updatedPage));
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update page: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}/toggle-favorite")
    public ResponseEntity<?> toggleFavorite(@PathVariable String id) {
        try {
            String result = pageService.toggleFavorite(id);

            if (result.equals("not found")) {
                return new ResponseEntity<>("Page not found", HttpStatus.NOT_FOUND);
            } else {
                Page page = pageService.getPageById(id);
                return ResponseEntity.ok(dtoMapper.toPageDTO(page));
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to toggle favorite: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}/toggle-archive")
    public ResponseEntity<?> toggleArchive(@PathVariable String id) {
        try {
            String result = pageService.toggleArchive(id);

            if (result.equals("not found")) {
                return new ResponseEntity<>("Page not found", HttpStatus.NOT_FOUND);
            } else {
                Page page = pageService.getPageById(id);
                return ResponseEntity.ok(dtoMapper.toPageDTO(page));
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to toggle archive: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}/move")
    public ResponseEntity<?> movePage(
            @PathVariable String id,
            @RequestParam String workspaceId) {
        try {
            String result = pageService.movePage(id, workspaceId);

            if (result.equals("page not found")) {
                return new ResponseEntity<>("Page not found", HttpStatus.NOT_FOUND);
            } else if (result.equals("workspace not found")) {
                return new ResponseEntity<>("Workspace not found", HttpStatus.NOT_FOUND);
            } else {
                Page page = pageService.getPageById(id);
                return ResponseEntity.ok(dtoMapper.toPageDTO(page));
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to move page: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePage(@PathVariable String id) {
        try {
            String result = pageService.deletePage(id);

            if (result.equals("not found")) {
                return new ResponseEntity<>("Page not found", HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<>("Page deleted successfully", HttpStatus.OK);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to delete page: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
}