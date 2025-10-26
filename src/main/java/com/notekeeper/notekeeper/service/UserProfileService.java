package com.notekeeper.notekeeper.service;

import com.notekeeper.notekeeper.model.UserProfile;
import com.notekeeper.notekeeper.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserProfileService {

    @Autowired
    private UserProfileRepository userProfileRepository;

    // CREATE
    public UserProfile createProfile(UserProfile profile) {
        return userProfileRepository.save(profile);
    }

    // READ
    public UserProfile getProfileById(String id) {
        return userProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found with id: " + id));
    }

    public UserProfile getProfileByUserId(String userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for user: " + userId));
    }

    public List<UserProfile> getAllProfiles() {
        return userProfileRepository.findAll();
    }

    public List<UserProfile> getProfilesByTheme(String theme) {
        return userProfileRepository.findByTheme(theme);
    }

    // UPDATE
    public UserProfile updateProfile(String id, UserProfile profileDetails) {
        UserProfile profile = getProfileById(id);
        profile.setBio(profileDetails.getBio());
        profile.setAvatarUrl(profileDetails.getAvatarUrl());
        profile.setTheme(profileDetails.getTheme());
        profile.setLanguage(profileDetails.getLanguage());
        return userProfileRepository.save(profile);
    }

    // DELETE
    public void deleteProfile(String id) {
        UserProfile profile = getProfileById(id);
        userProfileRepository.delete(profile);
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