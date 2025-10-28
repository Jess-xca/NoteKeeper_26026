package com.notekeeper.notekeeper.service;

import com.notekeeper.notekeeper.model.UserProfile;
import com.notekeeper.notekeeper.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserProfileService {

    @Autowired
    private UserProfileRepository userProfileRepository;

    // CREATE
    public String createProfile(UserProfile profile) {
        try {
            UserProfile saved = userProfileRepository.save(profile);
            return saved.getId();
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    // READ
    public UserProfile getProfileById(String id) {
        Optional<UserProfile> profile = userProfileRepository.findById(id);
        return profile.orElse(null);
    }

    public UserProfile getProfileByUserId(String userId) {
        Optional<UserProfile> profile = userProfileRepository.findByUserId(userId);
        return profile.orElse(null);
    }

    public List<UserProfile> getAllProfiles() {
        return userProfileRepository.findAll();
    }

    public List<UserProfile> getProfilesByTheme(String theme) {
        return userProfileRepository.findByTheme(theme);
    }

    // UPDATE
    public String updateProfile(String id, UserProfile profileDetails) {
        Optional<UserProfile> profileOpt = userProfileRepository.findById(id);

        if (!profileOpt.isPresent()) {
            return "not found";
        }

        UserProfile profile = profileOpt.get();
        profile.setBio(profileDetails.getBio());
        profile.setAvatarUrl(profileDetails.getAvatarUrl());
        profile.setTheme(profileDetails.getTheme());
        profile.setLanguage(profileDetails.getLanguage());
        userProfileRepository.save(profile);
        return "success";
    }

    // DELETE
    public String deleteProfile(String id) {
        Optional<UserProfile> profileOpt = userProfileRepository.findById(id);

        if (!profileOpt.isPresent()) {
            return "not found";
        }

        userProfileRepository.delete(profileOpt.get());
        return "success";
    }

    // PAGINATION
    public Page<UserProfile> getProfilesPaginated(int page, int size) {
        return userProfileRepository.findAll(PageRequest.of(page, size));
    }

    // STATISTICS
    public long countProfiles() {
        return userProfileRepository.count();
    }

    public long countByTheme(String theme) {
        return userProfileRepository.countByTheme(theme);
    }
}