package com.notekeeper.notekeeper.service;

import com.notekeeper.notekeeper.model.UserProfile;
import com.notekeeper.notekeeper.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import com.notekeeper.notekeeper.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

    @Autowired
    private UserProfileRepository userProfileRepository;

    // CREATE
    @Transactional
    public String createProfile(UserProfile profile) {
        UserProfile saved = userProfileRepository.save(profile);
        return saved.getId();
    }

    // READ
    public UserProfile getProfileById(String id) {
        return userProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
    }

    public UserProfile getProfileByUserId(String userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user"));
    }

    public List<UserProfile> getAllProfiles() {
        return userProfileRepository.findAll();
    }

    public List<UserProfile> getProfilesByTheme(String theme) {
        return userProfileRepository.findByTheme(theme);
    }

    // UPDATE
    @Transactional
    public void updateProfile(String id, UserProfile profileDetails) {
        UserProfile profile = userProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        profile.setBio(profileDetails.getBio());
        profile.setAvatarUrl(profileDetails.getAvatarUrl());
        profile.setTheme(profileDetails.getTheme());
        profile.setLanguage(profileDetails.getLanguage());
        userProfileRepository.save(profile);
    }

    // DELETE
    @Transactional
    public void deleteProfile(String id) {
        UserProfile profile = userProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

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
