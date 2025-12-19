package com.notekeeper.notekeeper.controller;

import com.notekeeper.notekeeper.model.Page;
import com.notekeeper.notekeeper.model.PageShare;
import com.notekeeper.notekeeper.model.User;
import com.notekeeper.notekeeper.repository.PageRepository;
import com.notekeeper.notekeeper.repository.PageShareRepository;
import com.notekeeper.notekeeper.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/shares")
@CrossOrigin(origins = "*")
public class PageShareController {

    @Autowired
    private PageShareRepository pageShareRepository;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private UserRepository userRepository;

    // CREATE - Share page with user by email
    @PostMapping
    public ResponseEntity<?> sharePage(@RequestBody Map<String, String> request) {
        try {
            String pageId = request.get("pageId");
            String sharedById = request.get("sharedById");
            String email = request.get("email");
            String permission = request.getOrDefault("permission", "VIEW");

            // Validate required fields
            if (pageId == null || sharedById == null || email == null) {
                return ResponseEntity.badRequest().body("pageId, sharedById, and email are required");
            }

            // Get page
            Optional<Page> pageOpt = pageRepository.findById(pageId);
            if (!pageOpt.isPresent()) {
                return ResponseEntity.badRequest().body("Page not found");
            }

            // Get sharing user
            Optional<User> sharedByOpt = userRepository.findById(sharedById);
            if (!sharedByOpt.isPresent()) {
                return ResponseEntity.badRequest().body("Sharing user not found");
            }

            // Get user to share with by email
            Optional<User> sharedWithOpt = userRepository.findByEmail(email);
            if (!sharedWithOpt.isPresent()) {
                return ResponseEntity.badRequest().body("User with email '" + email + "' not found");
            }

            User sharedWith = sharedWithOpt.get();

            // Check if user is trying to share with themselves
            if (sharedWith.getId().equals(sharedById)) {
                return ResponseEntity.badRequest().body("Cannot share with yourself");
            }

            // Check if already shared
            Optional<PageShare> existingShare = pageShareRepository.findByPageIdAndSharedWithId(pageId, sharedWith.getId());
            if (existingShare.isPresent()) {
                return ResponseEntity.badRequest().body("Page already shared with this user");
            }

            // Create share
            PageShare share = new PageShare(pageOpt.get(), sharedByOpt.get(), sharedWith, permission);
            PageShare saved = pageShareRepository.save(share);

            // Return response with user info
            Map<String, Object> response = new HashMap<>();
            response.put("id", saved.getId());
            response.put("pageId", pageId);
            response.put("sharedWith", Map.of(
                "id", sharedWith.getId(),
                "email", sharedWith.getEmail(),
                "firstName", sharedWith.getFirstName(),
                "lastName", sharedWith.getLastName()
            ));
            response.put("permission", saved.getPermission());
            response.put("sharedAt", saved.getSharedAt());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to share page: " + e.getMessage());
        }
    }

    // READ - Get all shares for a page
    @GetMapping("/page/{pageId}")
    public ResponseEntity<?> getPageShares(@PathVariable String pageId) {
        try {
            List<PageShare> shares = pageShareRepository.findByPageId(pageId);
            return ResponseEntity.ok(shares);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to get shares: " + e.getMessage());
        }
    }

    // READ - Get all pages shared with a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getSharedWithUser(@PathVariable String userId) {
        try {
            List<PageShare> shares = pageShareRepository.findBySharedWithId(userId);
            return ResponseEntity.ok(shares);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to get shared pages: " + e.getMessage());
        }
    }

    // READ - Get all pages shared by a user
    @GetMapping("/shared-by/{userId}")
    public ResponseEntity<?> getSharedByUser(@PathVariable String userId) {
        try {
            List<PageShare> shares = pageShareRepository.findBySharedById(userId);
            return ResponseEntity.ok(shares);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to get shared pages: " + e.getMessage());
        }
    }

    // UPDATE - Change share permission
    @PutMapping("/{shareId}")
    public ResponseEntity<?> updateSharePermission(@PathVariable String shareId, @RequestBody Map<String, String> request) {
        try {
            Optional<PageShare> shareOpt = pageShareRepository.findById(shareId);
            if (!shareOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            String permission = request.get("permission");
            if (permission == null || (!permission.equals("VIEW") && !permission.equals("EDIT"))) {
                return ResponseEntity.badRequest().body("Permission must be VIEW or EDIT");
            }

            PageShare share = shareOpt.get();
            share.setPermission(permission);
            PageShare updated = pageShareRepository.save(share);

            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update share: " + e.getMessage());
        }
    }

    // DELETE - Remove share
    @DeleteMapping("/{shareId}")
    public ResponseEntity<?> removeShare(@PathVariable String shareId) {
        try {
            Optional<PageShare> shareOpt = pageShareRepository.findById(shareId);
            if (!shareOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            pageShareRepository.delete(shareOpt.get());
            return ResponseEntity.ok("Share removed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to remove share: " + e.getMessage());
        }
    }
}
