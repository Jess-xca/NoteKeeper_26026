package com.notekeeper.notekeeper.service;

import com.notekeeper.notekeeper.model.User;
import com.notekeeper.notekeeper.model.UserProfile;
import com.notekeeper.notekeeper.model.Workspace;
import com.notekeeper.notekeeper.repository.UserRepository;
import com.notekeeper.notekeeper.repository.UserProfileRepository;
import com.notekeeper.notekeeper.repository.WorkspaceRepository;
import com.notekeeper.notekeeper.repository.LocationRepository;
import com.notekeeper.notekeeper.dto.UserDTO;
import com.notekeeper.notekeeper.dto.UserProfileDTO;
import com.notekeeper.notekeeper.mapper.DTOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private DTOMapper dtoMapper;

    // CREATE
    public User createUser(User user) {
        return createAndPersistUser(user, null);
    }

    // DTO-based create: accepts UserDTO, maps nested location/profile when provided
    public UserDTO createUser(UserDTO userDTO) {
        if (userDTO == null)
            throw new RuntimeException("Invalid user data");
        User user = dtoMapper.toUserEntity(userDTO);

        // If location provided with id, fetch and set on the entity before persisting
        if (userDTO.getLocation() != null && userDTO.getLocation().getId() != null) {
            var locOpt = locationRepository.findById(userDTO.getLocation().getId());
            if (!locOpt.isPresent()) {
                throw new RuntimeException("Location not found");
            }
            user.setLocation(locOpt.get());
        }

        User saved = createAndPersistUser(user, userDTO.getProfile());
        return dtoMapper.toUserDTO(saved);
    }

    // central helper to persist a new user and its default related entities; avoids
    // duplicating logic
    private User createAndPersistUser(User user, UserProfileDTO profileDTO) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User savedUser = userRepository.save(user);

        // Create default profile
        UserProfile profile = new UserProfile(savedUser);
        if (profileDTO != null) {
            if (profileDTO.getBio() != null)
                profile.setBio(profileDTO.getBio());
            if (profileDTO.getAvatarUrl() != null)
                profile.setAvatarUrl(profileDTO.getAvatarUrl());
            if (profileDTO.getTheme() != null)
                profile.setTheme(profileDTO.getTheme());
            if (profileDTO.getLanguage() != null)
                profile.setLanguage(profileDTO.getLanguage());
        }
        userProfileRepository.save(profile);
        // ensure bi-directional association is set on the user object
        savedUser.setProfile(profile);
        // persist the user again so the relationship is visible on the returned entity
        userRepository.save(savedUser);

        // Create default Inbox workspace
        Workspace inbox = new Workspace("Inbox", savedUser, true);
        inbox.setDescription("Quick capture notes");
        inbox.setIcon("📥");
        workspaceRepository.save(inbox);

        return savedUser;
    }

    // READ
    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public UserDTO getUserByIdDTO(String id) {
        User user = getUserById(id);
        return dtoMapper.toUserDTO(user);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // UPDATE - for admin panel (selective fields)
    public User updateUserAdmin(String id, User updatedUser) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Update allowed fields
        if (updatedUser.getFirstName() != null) {
            existingUser.setFirstName(updatedUser.getFirstName());
        }
        if (updatedUser.getLastName() != null) {
            existingUser.setLastName(updatedUser.getLastName());
        }
        if (updatedUser.getPhoneNumber() != null) {
            existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
        }
        if (updatedUser.getRole() != null) {
            existingUser.setRole(updatedUser.getRole());
        }
        if (updatedUser.getGender() != null) {
            existingUser.setGender(updatedUser.getGender());
        }

        return userRepository.save(existingUser);
    }

    public List<UserDTO> getAllUsersDTO() {
        return getAllUsers().stream().map(dtoMapper::toUserDTO).toList();
    }

    public List<User> getUsersByLocationName(String locationName) {
        return userRepository.findByLocationName(locationName);
    }

    public List<User> getUsersByProvinceCode(String provinceCode) {
        return userRepository.findByLocationCodePrefix(provinceCode);
    }

    public UserDTO getUserByUsernameDTO(String username) {
        User user = getUserByUsername(username);
        return dtoMapper.toUserDTO(user);
    }

    public List<User> searchUsers(String keyword) {
        return userRepository.searchUsers(keyword);
    }

    // DTO-based search and location listing
    public List<UserDTO> getUsersByLocationDTO(String locationName) {
        return getUsersByLocationName(locationName).stream().map(dtoMapper::toUserDTO).toList();
    }

    public List<UserDTO> searchUsersDTO(String keyword) {
        return searchUsers(keyword).stream().map(dtoMapper::toUserDTO).toList();
    }

    public List<UserDTO> getUsersByProvinceCodeDTO(String code) {
        return getUsersByProvinceCode(code).stream().map(dtoMapper::toUserDTO).toList();
    }

    // DTO-based update: merge allowed fields and nested profile/location
    public UserDTO updateUser(String id, UserDTO userDTO) {
        User user = getUserById(id);

        if (userDTO.getEmail() != null && !userDTO.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
            user.setEmail(userDTO.getEmail());
        }

        if (userDTO.getFirstName() != null)
            user.setFirstName(userDTO.getFirstName());
        if (userDTO.getLastName() != null)
            user.setLastName(userDTO.getLastName());

        // Location mapping: if provided and has id, set existing location
        if (userDTO.getLocation() != null && userDTO.getLocation().getId() != null) {
            var locOpt = locationRepository.findById(userDTO.getLocation().getId());
            if (!locOpt.isPresent()) {
                throw new RuntimeException("Location not found");
            }
            user.setLocation(locOpt.get());
        }

        User saved = userRepository.save(user);

        // Update profile if provided
        if (userDTO.getProfile() != null) {
            UserProfile profile = saved.getProfile();
            if (profile == null) {
                profile = new UserProfile(saved);
            }
            UserProfileDTO p = userDTO.getProfile();
            if (p.getBio() != null)
                profile.setBio(p.getBio());
            if (p.getAvatarUrl() != null)
                profile.setAvatarUrl(p.getAvatarUrl());
            if (p.getTheme() != null)
                profile.setTheme(p.getTheme());
            if (p.getLanguage() != null)
                profile.setLanguage(p.getLanguage());
            userProfileRepository.save(profile);
        }

        return dtoMapper.toUserDTO(saved);
    }

    // DELETE
    public void deleteUser(String id) {
        User user = getUserById(id);

        // First, delete all workspaces owned by this user (cascade will handle
        // workspace members)
        List<Workspace> ownedWorkspaces = workspaceRepository.findByOwnerId(user.getId());
        if (ownedWorkspaces != null && !ownedWorkspaces.isEmpty()) {
            workspaceRepository.deleteAll(ownedWorkspaces);
        }

        // Now safe to delete the user (workspace_members FK will be handled by
        // cascading)
        userRepository.delete(user);
    }

    // DTO-based delete: keep behavior same but expose DTO-friendly method
    public void deleteUserDTO(String id) {
        deleteUser(id);
    }

    // SORTING
    public List<User> getUsersSorted(String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return userRepository.findAll(sort);
    }

    // PAGINATION
    public Page<User> getUsersPaginated(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size));
    }

    public Page<UserDTO> getUsersPaginatedDTO(int page, int size) {
        return getUsersPaginated(page, size).map(dtoMapper::toUserDTO);
    }

    public Page<User> getUsersByLocationPaginated(String locationName, int page, int size) {
        return userRepository.findByLocationNamePaginated(locationName, PageRequest.of(page, size));
    }

    // STATISTICS
    public long countUsers() {
        return userRepository.count();
    }

    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }
}