package com.notekeeper.notekeeper.controller;

import com.notekeeper.notekeeper.dto.UserDTO;
import com.notekeeper.notekeeper.dto.ChangePasswordRequest;
import com.notekeeper.notekeeper.mapper.DTOMapper;
import com.notekeeper.notekeeper.model.User;
import com.notekeeper.notekeeper.repository.UserRepository;
import com.notekeeper.notekeeper.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DTOMapper dtoMapper;

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO) {
        try {
            UserDTO created = userService.createUser(userDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("Username already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already taken");
            } else if (msg != null && msg.contains("Email already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already registered");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to create user: "
                                + (e.getMessage() != null ? e.getMessage() : "unexpected error"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create user: " + (e.getMessage() != null ? e.getMessage() : "unexpected error"));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            List<UserDTO> users = userService.getAllUsersDTO();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch users: " + (e.getMessage() != null ? e.getMessage() : "unexpected error"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        try {
            UserDTO user = userService.getUserByIdDTO(id);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with id: " + id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch user: " + (e.getMessage() != null ? e.getMessage() : "unexpected error"));
        }
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        try {
            UserDTO user = userService.getUserByUsernameDTO(username);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with username: " + username);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch user: " + (e.getMessage() != null ? e.getMessage() : "unexpected error"));
        }
    }

    @GetMapping("/by-location")
    public ResponseEntity<?> getUsersByLocation(@RequestParam String locationName) {
        try {
            List<UserDTO> users = userService.getUsersByLocationDTO(locationName);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch users by location '" + locationName + "': "
                            + (e.getMessage() != null ? e.getMessage() : "unexpected error"));
        }
    }

    @GetMapping("/by-province-code")
    public ResponseEntity<?> getUsersByProvinceCode(@RequestParam String code) {
        try {
            List<UserDTO> users = userService.getUsersByProvinceCodeDTO(code);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch users by province code '" + code + "': "
                            + (e.getMessage() != null ? e.getMessage() : "unexpected error"));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam String keyword) {
        try {
            List<UserDTO> users = userService.searchUsersDTO(keyword);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to search users with keyword '" + keyword + "': "
                            + (e.getMessage() != null ? e.getMessage() : "unexpected error"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody UserDTO userDTO) {
        try {
            UserDTO updated = userService.updateUser(id, userDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with id: " + id);
            } else if (msg != null && msg.contains("Email already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already taken by another user");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to update user: "
                                + (e.getMessage() != null ? e.getMessage() : "unexpected error"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update user: " + (e.getMessage() != null ? e.getMessage() : "unexpected error"));
        }
    }

    // Admin-specific update endpoint (selective fields)
    @PutMapping("/admin/{id}")
    public ResponseEntity<?> updateUserAdmin(@PathVariable String id, @RequestBody User user) {
        try {
            User updatedUser = userService.updateUserAdmin(id, user);
            return ResponseEntity.ok(dtoMapper.toUserDTO(updatedUser));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update user: " + (e.getMessage() != null ? e.getMessage() : "unexpected error"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        try {
            userService.deleteUserDTO(id);
            return ResponseEntity.ok("User deleted successfully");
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with id: " + id);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to delete user: "
                                + (e.getMessage() != null ? e.getMessage() : "unexpected error"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete user: " + (e.getMessage() != null ? e.getMessage() : "unexpected error"));
        }
    }

    @GetMapping("/paginated")
    public ResponseEntity<?> getUsersPaginated(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<UserDTO> users = userService.getUsersPaginatedDTO(page, size);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch paginated users: "
                            + (e.getMessage() != null ? e.getMessage() : "unexpected error"));
        }
    }

    @GetMapping("/sorted")
    public ResponseEntity<?> getUsersSorted(@RequestParam String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        try {
            List<UserDTO> users = userService.getUsersSorted(sortBy, direction).stream()
                    .map(dtoMapper::toUserDTO)
                    .toList();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch sorted users: "
                            + (e.getMessage() != null ? e.getMessage() : "unexpected error"));
        }
    }

    @GetMapping("/count")
    public ResponseEntity<?> countUsers() {
        try {
            long count = userService.countUsers();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to count users: " + (e.getMessage() != null ? e.getMessage() : "unexpected error"));
        }
    }

    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        try {
            boolean available = userService.isUsernameAvailable(username);
            return ResponseEntity.ok(available);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to check username availability: "
                            + (e.getMessage() != null ? e.getMessage() : "unexpected error"));
        }
    }

    @PutMapping("/{id}/change-password")
    public ResponseEntity<?> changePassword(@PathVariable String id, @RequestBody ChangePasswordRequest request) {
        try {
            // Validate input
            if (request.getCurrentPassword() == null || request.getCurrentPassword().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Current password is required");
            }
            if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("New password is required");
            }
            if (request.getNewPassword().length() < 8) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password must be at least 8 characters");
            }

            // Get user
            User user = userService.getUserById(id);

            // Verify current password (plain text comparison - NOT SECURE in production!)
            // In production, use BCrypt: passwordEncoder.matches(currentPassword,
            // user.getPassword())
            if (!user.getPassword().equals(request.getCurrentPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Current password is incorrect");
            }

            // Update password (in production, hash with BCrypt before saving)
            user.setPassword(request.getNewPassword());
            userRepository.save(user);

            return ResponseEntity.ok("Password changed successfully");
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with id: " + id);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to change password: "
                                + (e.getMessage() != null ? e.getMessage() : "unexpected error"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to change password: "
                            + (e.getMessage() != null ? e.getMessage() : "unexpected error"));
        }
    }
}