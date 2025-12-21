package com.notekeeper.notekeeper.service;

import com.notekeeper.notekeeper.model.User;
import com.notekeeper.notekeeper.model.UserProfile;
import com.notekeeper.notekeeper.model.Workspace;
import com.notekeeper.notekeeper.model.WorkspaceMember;
import com.notekeeper.notekeeper.model.WorkspaceRole;
import com.notekeeper.notekeeper.repository.WorkspaceMemberRepository;
import com.notekeeper.notekeeper.repository.UserRepository;
import com.notekeeper.notekeeper.repository.UserProfileRepository;
import com.notekeeper.notekeeper.repository.WorkspaceRepository;
import com.notekeeper.notekeeper.repository.LocationRepository;
import com.notekeeper.notekeeper.repository.PasswordResetTokenRepository;
import com.notekeeper.notekeeper.model.PasswordResetToken;
import com.notekeeper.notekeeper.dto.UserDTO;
import com.notekeeper.notekeeper.dto.UserProfileDTO;
import com.notekeeper.notekeeper.mapper.DTOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.notekeeper.notekeeper.exception.ResourceNotFoundException;
import com.notekeeper.notekeeper.exception.BadRequestException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
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
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Autowired
    private LocationRepository locationRepository;
    
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DTOMapper dtoMapper;

    // CREATE
    @Transactional
    public User createUser(User user) {
        return createAndPersistUser(user, null);
    }

    // DTO-based create: accepts UserDTO, maps nested location/profile when provided
    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        if (userDTO == null)
            throw new BadRequestException("Invalid user data");
        User user = dtoMapper.toUserEntity(userDTO);

        // If location provided with id, fetch and set on the entity before persisting
        if (userDTO.getLocation() != null && userDTO.getLocation().getId() != null) {
            var loc = locationRepository.findById(userDTO.getLocation().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Location not found"));
            user.setLocation(loc);
        }

        User saved = createAndPersistUser(user, userDTO.getProfile());
        return dtoMapper.toUserDTO(saved);
    }

    // central helper to persist a new user and its default related entities
    private User createAndPersistUser(User user, UserProfileDTO profileDTO) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new BadRequestException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
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
        savedUser.setProfile(profile);
        userRepository.save(savedUser);

        // Create default Inbox workspace
        Workspace inbox = new Workspace("Inbox", savedUser, true);
        inbox.setDescription("Quick capture notes");
        inbox.setIcon("📥");
        workspaceRepository.save(inbox);

        // Add owner as a member
        WorkspaceMember member = new WorkspaceMember(inbox, savedUser, WorkspaceRole.OWNER);
        workspaceMemberRepository.save(member);

        return savedUser;
    }

    // READ
    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public UserDTO getUserByIdDTO(String id) {
        User user = getUserById(id);
        return dtoMapper.toUserDTO(user);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // UPDATE - for admin panel (selective fields)
    @Transactional
    public User updateUserAdmin(String id, User updatedUser) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

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
    @Transactional
    public UserDTO updateUser(String id, UserDTO userDTO) {
        User user = getUserById(id);

        if (userDTO.getEmail() != null && !userDTO.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                throw new BadRequestException("Email already exists");
            }
            user.setEmail(userDTO.getEmail());
        }

        if (userDTO.getFirstName() != null)
            user.setFirstName(userDTO.getFirstName());
        if (userDTO.getLastName() != null)
            user.setLastName(userDTO.getLastName());

        // Location mapping
        if (userDTO.getLocation() != null && userDTO.getLocation().getId() != null) {
            var loc = locationRepository.findById(userDTO.getLocation().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Location not found"));
            user.setLocation(loc);
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
    @Transactional
    public void deleteUser(String id) {
        User user = getUserById(id);

        // Delete owned workspaces
        List<Workspace> ownedWorkspaces = workspaceRepository.findByOwnerId(user.getId());
        if (ownedWorkspaces != null && !ownedWorkspaces.isEmpty()) {
            workspaceRepository.deleteAll(ownedWorkspaces);
        }

        userRepository.delete(user);
    }

    // DTO-based delete
    @Transactional
    public void deleteUserDTO(String id) {
        deleteUser(id);
    }

    @Transactional
    public void changePassword(String id, String currentPassword, String newPassword) {
        User user = getUserById(id);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        if (newPassword.length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        if (token == null || newPassword == null || newPassword.trim().isEmpty()) {
            throw new BadRequestException("Token and new password are required");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (resetToken.isExpired() || resetToken.isUsed()) {
            throw new BadRequestException("Invalid or expired reset token");
        }

        User user = resetToken.getUser();
        System.out.println("🔄 Service: Resetting password for user: " + user.getUsername() + " (ID: " + user.getId() + ")");
        
        // Encode and update password
        String encodedPassword = passwordEncoder.encode(newPassword.trim());
        user.setPassword(encodedPassword);
        
        // Explicit save and flush to ensure it hits the DB
        userRepository.saveAndFlush(user);

        // Mark token as used
        resetToken.setUsed(true);
        passwordResetTokenRepository.saveAndFlush(resetToken);
        
        System.out.println("✅ Service: Password reset successfully. New hash snippet: " + encodedPassword.substring(0, 10) + "...");
        
        // Double check validation immediately after save in the same transaction
        boolean matches = passwordEncoder.matches(newPassword.trim(), user.getPassword());
        System.out.println("🧪 Service Verification: Does password match immediately? " + matches);
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