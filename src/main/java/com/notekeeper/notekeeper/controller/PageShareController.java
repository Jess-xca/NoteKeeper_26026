package com.notekeeper.notekeeper.controller;

import com.notekeeper.notekeeper.model.PageShare;
import com.notekeeper.notekeeper.service.PageShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shares")
@CrossOrigin(origins = "*")
public class PageShareController {

    @Autowired
    private PageShareService pageShareService;

    // CREATE - Share page with user by email
    @PostMapping
    public ResponseEntity<Map<String, Object>> sharePage(@RequestBody Map<String, String> request) {
        String pageId = request.get("pageId");
        String sharedById = request.get("sharedById");
        String email = request.get("email");
        String permission = request.getOrDefault("permission", "VIEW");

        PageShare saved = pageShareService.sharePage(pageId, sharedById, email, permission);

        Map<String, Object> response = new HashMap<>();
        response.put("id", saved.getId());
        response.put("pageId", saved.getPage().getId());
        response.put("sharedWith", Map.of(
            "id", saved.getSharedWith().getId(),
            "email", saved.getSharedWith().getEmail(),
            "firstName", saved.getSharedWith().getFirstName(),
            "lastName", saved.getSharedWith().getLastName()
        ));
        response.put("permission", saved.getPermission());
        response.put("sharedAt", saved.getSharedAt());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // READ - Get all shares for a page
    @GetMapping("/page/{pageId}")
    public ResponseEntity<List<PageShare>> getPageShares(@PathVariable String pageId) {
        List<PageShare> shares = pageShareService.getPageShares(pageId);
        return ResponseEntity.ok(shares);
    }

    // READ - Get all pages shared with a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PageShare>> getSharedWithUser(@PathVariable String userId) {
        List<PageShare> shares = pageShareService.getSharedWithUser(userId);
        return ResponseEntity.ok(shares);
    }

    // READ - Get all pages shared by a user
    @GetMapping("/shared-by/{userId}")
    public ResponseEntity<List<PageShare>> getSharedByUser(@PathVariable String userId) {
        List<PageShare> shares = pageShareService.getSharedByUser(userId);
        return ResponseEntity.ok(shares);
    }

    // UPDATE - Change share permission
    @PutMapping("/{shareId}")
    public ResponseEntity<PageShare> updateSharePermission(@PathVariable String shareId, @RequestBody Map<String, String> request) {
        String permission = request.get("permission");
        PageShare updated = pageShareService.updateSharePermission(shareId, permission);
        return ResponseEntity.ok(updated);
    }

    // DELETE - Remove share
    @DeleteMapping("/{shareId}")
    public ResponseEntity<Map<String, String>> removeShare(@PathVariable String shareId) {
        pageShareService.removeShare(shareId);
        return ResponseEntity.ok(Map.of("message", "Share removed successfully"));
    }
}
