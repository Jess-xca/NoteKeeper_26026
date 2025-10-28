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
    public ResponseEntity<?> createProfile(@RequestBody UserProfile profile) {
        try {
            String result = userProfileService.createProfile(profile);

            if (result.startsWith("error:")) {
                return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                UserProfile createdProfile = userProfileService.getProfileById(result);
                return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.toUserProfileDTO(createdProfile));
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create profile: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllProfiles() {
        try {
            List<UserProfile> profiles = userProfileService.getAllProfiles();
            List<UserProfileDTO> profileDTOs = profiles.stream()
                    .map(dtoMapper::toUserProfileDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(profileDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch profiles: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProfileById(@PathVariable String id) {
        try {
            UserProfile profile = userProfileService.getProfileById(id);
            if (profile == null) {
                return new ResponseEntity<>("Profile not found", HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok(dtoMapper.toUserProfileDTO(profile));
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch profile: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getProfileByUserId(@PathVariable String userId) {
        try {
            UserProfile profile = userProfileService.getProfileByUserId(userId);
            if (profile == null) {
                return new ResponseEntity<>("Profile not found for this user", HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok(dtoMapper.toUserProfileDTO(profile));
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch profile: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/theme/{theme}")
    public ResponseEntity<?> getProfilesByTheme(@PathVariable String theme) {
        try {
            List<UserProfile> profiles = userProfileService.getProfilesByTheme(theme);
            List<UserProfileDTO> profileDTOs = profiles.stream()
                    .map(dtoMapper::toUserProfileDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(profileDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch profiles by theme: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable String id, @RequestBody UserProfile profile) {
        try {
            String result = userProfileService.updateProfile(id, profile);

            if (result.equals("not found")) {
                return new ResponseEntity<>("Profile not found", HttpStatus.NOT_FOUND);
            } else {
                UserProfile updatedProfile = userProfileService.getProfileById(id);
                return ResponseEntity.ok(dtoMapper.toUserProfileDTO(updatedProfile));
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update profile: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProfile(@PathVariable String id) {
        try {
            String result = userProfileService.deleteProfile(id);

            if (result.equals("not found")) {
                return new ResponseEntity<>("Profile not found", HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<>("Profile deleted successfully", HttpStatus.OK);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to delete profile: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/paginated")
    public ResponseEntity<?> getProfilesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<UserProfile> profiles = userProfileService.getProfilesPaginated(page, size);
            Page<UserProfileDTO> profileDTOs = profiles.map(dtoMapper::toUserProfileDTO);
            return ResponseEntity.ok(profileDTOs);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to fetch paginated profiles: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/count")
    public ResponseEntity<?> countProfiles() {
        try {
            long count = userProfileService.countProfiles();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to count profiles: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/count/theme/{theme}")
    public ResponseEntity<?> countByTheme(@PathVariable String theme) {
        try {
            long count = userProfileService.countByTheme(theme);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to count profiles by theme: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}