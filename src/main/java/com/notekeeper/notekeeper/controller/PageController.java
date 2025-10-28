package com.notekeeper.notekeeper.controller;

import com.notekeeper.notekeeper.model.Page;
import com.notekeeper.notekeeper.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pages")
@CrossOrigin(origins = "*")
public class PageController {

    @Autowired
    private PageService pageService;

    // CREATE
    @PostMapping
    public ResponseEntity<Page> createPage(@RequestBody Page page) {
        try {
            Page createdPage = pageService.createPage(page);
            return new ResponseEntity<>(createdPage, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/quick-note")
    public ResponseEntity<Page> createQuickNote(
            @RequestParam String userId,
            @RequestParam String title,
            @RequestParam String content) {
        try {
            Page quickNote = pageService.createQuickNote(userId, title, content);
            return new ResponseEntity<>(quickNote, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // READ
    @GetMapping("/{id}")
    public ResponseEntity<Page> getPageById(@PathVariable String id) {
        try {
            Page page = pageService.getPageById(id);
            return new ResponseEntity<>(page, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<List<Page>> getAllPages() {
        try {
            List<Page> pages = pageService.getAllPages();
            return new ResponseEntity<>(pages, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Page>> getPagesByUser(@PathVariable String userId) {
        try {
            List<Page> pages = pageService.getPagesByUser(userId);
            return new ResponseEntity<>(pages, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<List<Page>> getPagesByWorkspace(@PathVariable String workspaceId) {
        try {
            List<Page> pages = pageService.getPagesByWorkspace(workspaceId);
            return new ResponseEntity<>(pages, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/favorites/{userId}")
    public ResponseEntity<List<Page>> getFavoritePages(@PathVariable String userId) {
        try {
            List<Page> pages = pageService.getFavoritePages(userId);
            return new ResponseEntity<>(pages, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/archived/{userId}")
    public ResponseEntity<List<Page>> getArchivedPages(@PathVariable String userId) {
        try {
            List<Page> pages = pageService.getArchivedPages(userId);
            return new ResponseEntity<>(pages, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/inbox/{userId}")
    public ResponseEntity<List<Page>> getInboxPages(@PathVariable String userId) {
        try {
            List<Page> pages = pageService.getInboxPages(userId);
            return new ResponseEntity<>(pages, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/recent/{userId}")
    public ResponseEntity<List<Page>> getRecentPages(@PathVariable String userId) {
        try {
            List<Page> pages = pageService.getRecentPages(userId);
            return new ResponseEntity<>(pages, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Page>> searchPages(@RequestParam String keyword) {
        try {
            List<Page> pages = pageService.searchPages(keyword);
            return new ResponseEntity<>(pages, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/search/user/{userId}")
    public ResponseEntity<List<Page>> searchUserPages(
            @PathVariable String userId,
            @RequestParam String keyword) {
        try {
            List<Page> pages = pageService.searchUserPages(userId, keyword);
            return new ResponseEntity<>(pages, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Page> updatePage(
            @PathVariable String id,
            @RequestBody Page pageDetails) {
        try {
            Page updatedPage = pageService.updatePage(id, pageDetails);
            return new ResponseEntity<>(updatedPage, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}/toggle-favorite")
    public ResponseEntity<Page> toggleFavorite(@PathVariable String id) {
        try {
            Page page = pageService.toggleFavorite(id);
            return new ResponseEntity<>(page, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}/toggle-archive")
    public ResponseEntity<Page> toggleArchive(@PathVariable String id) {
        try {
            Page page = pageService.toggleArchive(id);
            return new ResponseEntity<>(page, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}/move")
    public ResponseEntity<Page> movePage(
            @PathVariable String id,
            @RequestParam String workspaceId) {
        try {
            Page page = pageService.movePage(id, workspaceId);
            return new ResponseEntity<>(page, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deletePage(@PathVariable String id) {
        try {
            pageService.deletePage(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // SORTING & PAGINATION
    @GetMapping("/user/{userId}/sorted")
    public ResponseEntity<List<Page>> getUserPagesSorted(
            @PathVariable String userId,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        try {
            List<Page> pages = pageService.getUserPagesSorted(userId, sortBy, direction);
            return new ResponseEntity<>(pages, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/paginated")
    public ResponseEntity<org.springframework.data.domain.Page<Page>> getPagesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            org.springframework.data.domain.Page<Page> pages = pageService.getPagesPaginated(page, size);
            return new ResponseEntity<>(pages, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/{userId}/paginated")
    public ResponseEntity<org.springframework.data.domain.Page<Page>> getUserPagesPaginated(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            org.springframework.data.domain.Page<Page> pages = pageService.getUserPagesPaginated(userId, page, size);
            return new ResponseEntity<>(pages, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // STATISTICS
    @GetMapping("/count/user/{userId}")
    public ResponseEntity<Long> countUserPages(@PathVariable String userId) {
        try {
            long count = pageService.countUserPages(userId);
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/count/workspace/{workspaceId}")
    public ResponseEntity<Long> countWorkspacePages(@PathVariable String workspaceId) {
        try {
            long count = pageService.countWorkspacePages(workspaceId);
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/count/favorites/{userId}")
    public ResponseEntity<Long> countFavoritePages(@PathVariable String userId) {
        try {
            long count = pageService.countFavoritePages(userId);
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}