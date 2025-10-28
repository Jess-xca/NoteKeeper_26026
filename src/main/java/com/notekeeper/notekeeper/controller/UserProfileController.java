package com.notekeeper.notekeeper.controller;

import com.notekeeper.notekeeper.model.UserProfile;
import com.notekeeper.notekeeper.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profiles")
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @PostMapping
    public ResponseEntity<UserProfile> createProfile(@RequestBody UserProfile profile) {
        UserProfile createdProfile = userProfileService.createProfile(profile);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProfile);
    }

    @GetMapping
    public ResponseEntity<List<UserProfile>> getAllProfiles() {
        List<UserProfile> profiles = userProfileService.getAllProfiles();
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfile> getProfileById(@PathVariable String id) {
        UserProfile profile = userProfileService.getProfileById(id);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserProfile> getProfileByUserId(@PathVariable String userId) {
        UserProfile profile = userProfileService.getProfileByUserId(userId);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/theme/{theme}")
    public ResponseEntity<List<UserProfile>> getProfilesByTheme(@PathVariable String theme) {
        List<UserProfile> profiles = userProfileService.getProfilesByTheme(theme);
        return ResponseEntity.ok(profiles);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserProfile> updateProfile(@PathVariable String id, @RequestBody UserProfile profile) {
        UserProfile updatedProfile = userProfileService.updateProfile(id, profile);
        return ResponseEntity.ok(updatedProfile);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable String id) {
        userProfileService.deleteProfile(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<UserProfile>> getProfilesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<UserProfile> profiles = userProfileService.getProfilesPaginated(page, size);
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countProfiles() {
        long count = userProfileService.countProfiles();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count/theme/{theme}")
    public ResponseEntity<Long> countByTheme(@PathVariable String theme) {
        long count = userProfileService.countByTheme(theme);
        return ResponseEntity.ok(count);
    }
}
