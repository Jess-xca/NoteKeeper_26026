package com.notekeeper.notekeeper.controller;

import com.notekeeper.notekeeper.dto.UserProfileDTO;
import com.notekeeper.notekeeper.mapper.DTOMapper;
import com.notekeeper.notekeeper.model.UserProfile;
import com.notekeeper.notekeeper.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/profiles")
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private DTOMapper dtoMapper;

    @PostMapping
    public ResponseEntity<UserProfileDTO> createProfile(@RequestBody UserProfile profile) {
        String resultId = userProfileService.createProfile(profile);
        UserProfile createdProfile = userProfileService.getProfileById(resultId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.toUserProfileDTO(createdProfile));
    }

    @GetMapping
    public ResponseEntity<List<UserProfileDTO>> getAllProfiles() {
        List<UserProfile> profiles = userProfileService.getAllProfiles();
        List<UserProfileDTO> profileDTOs = profiles.stream()
                .map(dtoMapper::toUserProfileDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(profileDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileDTO> getProfileById(@PathVariable String id) {
        UserProfile profile = userProfileService.getProfileById(id);
        return ResponseEntity.ok(dtoMapper.toUserProfileDTO(profile));
    }

    @GetMapping("/my")
    public ResponseEntity<UserProfileDTO> getMyProfile(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.notekeeper.notekeeper.security.UserPrincipal principal) {
        UserProfile profile = userProfileService.getProfileByUserId(principal.getId());
        return ResponseEntity.ok(dtoMapper.toUserProfileDTO(profile));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserProfileDTO> getProfileByUserId(@PathVariable String userId) {
        UserProfile profile = userProfileService.getProfileByUserId(userId);
        return ResponseEntity.ok(dtoMapper.toUserProfileDTO(profile));
    }

    @GetMapping("/theme/{theme}")
    public ResponseEntity<List<UserProfileDTO>> getProfilesByTheme(@PathVariable String theme) {
        List<UserProfile> profiles = userProfileService.getProfilesByTheme(theme);
        List<UserProfileDTO> profileDTOs = profiles.stream()
                .map(dtoMapper::toUserProfileDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(profileDTOs);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserProfileDTO> updateProfile(@PathVariable String id, @RequestBody UserProfile profile) {
        userProfileService.updateProfile(id, profile);
        UserProfile updatedProfile = userProfileService.getProfileById(id);
        return ResponseEntity.ok(dtoMapper.toUserProfileDTO(updatedProfile));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<java.util.Map<String, String>> deleteProfile(@PathVariable String id) {
        userProfileService.deleteProfile(id);
        return ResponseEntity.ok(java.util.Map.of("message", "Profile deleted successfully"));
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<UserProfileDTO>> getProfilesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<UserProfile> profiles = userProfileService.getProfilesPaginated(page, size);
        Page<UserProfileDTO> profileDTOs = profiles.map(dtoMapper::toUserProfileDTO);
        return ResponseEntity.ok(profileDTOs);
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