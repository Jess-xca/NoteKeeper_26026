package com.notekeeper.notekeeper.controller;

import com.notekeeper.notekeeper.model.PageShare;
import com.notekeeper.notekeeper.security.UserPrincipal;
import com.notekeeper.notekeeper.service.PageShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Direct page shares. Every call is made as the signed-in user: only the page's
 * author (or its workspace owner) can share, change or remove shares, and the
 * sharer is never taken from the request body.
 */
@RestController
@RequestMapping("/api/shares")
@CrossOrigin(origins = "*")
public class PageShareController {

    @Autowired
    private PageShareService pageShareService;

    // CREATE - Share page with user by email
    @PostMapping
    public ResponseEntity<Map<String, Object>> sharePage(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> request) {
        String pageId = request.get("pageId");
        String email = request.get("email");
        String permission = request.getOrDefault("permission", "VIEW");

        PageShare saved = pageShareService.sharePage(pageId, principal.getId(), email, permission);

        Map<String, Object> response = new HashMap<>();
        response.put("id", saved.getId());
        response.put("pageId", saved.getPage().getId());
        response.put("sharedWith", Map.of(
                "id", saved.getSharedWith().getId(),
                "email", saved.getSharedWith().getEmail(),
                "firstName", saved.getSharedWith().getFirstName(),
                "lastName", saved.getSharedWith().getLastName()));
        response.put("permission", saved.getPermission());
        response.put("sharedAt", saved.getSharedAt());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // READ - Get all shares for a page (page managers only)
    @GetMapping("/page/{pageId}")
    public ResponseEntity<List<PageShare>> getPageShares(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String pageId) {
        return ResponseEntity.ok(pageShareService.getPageShares(pageId, principal.getId()));
    }

    // READ - Pages shared with me
    @GetMapping("/mine")
    public ResponseEntity<List<PageShare>> getSharedWithMe(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(pageShareService.getSharedWithUser(principal.getId()));
    }

    // READ - Pages I shared
    @GetMapping("/shared-by-me")
    public ResponseEntity<List<PageShare>> getSharedByMe(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(pageShareService.getSharedByUser(principal.getId()));
    }

    // UPDATE - Change share permission
    @PutMapping("/{shareId}")
    public ResponseEntity<PageShare> updateSharePermission(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String shareId,
            @RequestBody Map<String, String> request) {
        String permission = request.get("permission");
        PageShare updated = pageShareService.updateSharePermission(shareId, permission, principal.getId());
        return ResponseEntity.ok(updated);
    }

    // DELETE - Remove share
    @DeleteMapping("/{shareId}")
    public ResponseEntity<Map<String, String>> removeShare(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String shareId) {
        pageShareService.removeShare(shareId, principal.getId());
        return ResponseEntity.ok(Map.of("message", "Share removed successfully"));
    }
}
