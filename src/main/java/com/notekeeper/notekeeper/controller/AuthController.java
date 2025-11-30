package com.notekeeper.notekeeper.controller;

import com.notekeeper.notekeeper.dto.*;
import com.notekeeper.notekeeper.mapper.DTOMapper;
import com.notekeeper.notekeeper.model.Location;
import com.notekeeper.notekeeper.model.User;
import com.notekeeper.notekeeper.model.UserProfile;
import com.notekeeper.notekeeper.model.Workspace;
import com.notekeeper.notekeeper.repository.LocationRepository;
import com.notekeeper.notekeeper.repository.UserRepository;
import com.notekeeper.notekeeper.repository.UserProfileRepository;
import com.notekeeper.notekeeper.repository.WorkspaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private DTOMapper dtoMapper;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Validate input
            if (loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new LoginResponse(false, "Username is required", null, null));
            }

            if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new LoginResponse(false, "Password is required", null, null));
            }

            // Find user by username
            Optional<User> userOpt = userRepository.findByUsername(loginRequest.getUsername());

            if (!userOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new LoginResponse(false, "Invalid username or password", null, null));
            }

            User user = userOpt.get();

            // NOTE: In production, use BCrypt or similar to hash and verify passwords
            // For now, we're doing simple string comparison (NOT SECURE!)
            if (!user.getPassword().equals(loginRequest.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new LoginResponse(false, "Invalid username or password", null, null));
            }

            // Generate a simple token (in production, use JWT)
            String token = "token_" + UUID.randomUUID().toString();

            // Convert to DTO
            UserDTO userDTO = dtoMapper.toUserDTO(user);

            // Return success response
            LoginResponse response = new LoginResponse(
                    true,
                    "Login successful",
                    token,
                    userDTO);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LoginResponse(false, "Login failed: " + e.getMessage(), null, null));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            // Validate required fields
            if (registerRequest.getUsername() == null || registerRequest.getUsername().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Username is required");
            }

            if (registerRequest.getEmail() == null || registerRequest.getEmail().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Email is required");
            }

            if (registerRequest.getPassword() == null || registerRequest.getPassword().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Password is required");
            }

            if (registerRequest.getFirstName() == null || registerRequest.getFirstName().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("First name is required");
            }

            if (registerRequest.getLastName() == null || registerRequest.getLastName().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Last name is required");
            }

            // Check if username already exists
            if (userRepository.existsByUsername(registerRequest.getUsername())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Username already exists");
            }

            // Check if email already exists
            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Email already exists");
            }

            // Create new user
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setEmail(registerRequest.getEmail());
            user.setPassword(registerRequest.getPassword()); // NOTE: Hash in production with BCrypt!
            user.setFirstName(registerRequest.getFirstName());
            user.setLastName(registerRequest.getLastName());
            user.setPhoneNumber(registerRequest.getPhoneNumber());
            user.setGender(registerRequest.getGender());
            user.setRole("USER"); // Default role

            // Parse and set date of birth if provided
            if (registerRequest.getDateOfBirth() != null && !registerRequest.getDateOfBirth().trim().isEmpty()) {
                try {
                    LocalDate dob = LocalDate.parse(registerRequest.getDateOfBirth());
                    user.setDateOfBirth(dob);
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Invalid date format. Use YYYY-MM-DD");
                }
            }

            // Set location if provided
            if (registerRequest.getLocationId() != null && !registerRequest.getLocationId().trim().isEmpty()) {
                Optional<Location> locationOpt = locationRepository.findById(registerRequest.getLocationId());
                if (!locationOpt.isPresent()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Invalid location ID");
                }
                user.setLocation(locationOpt.get());
            }

            // Save user
            User savedUser = userRepository.save(user);

            // Create default profile
            UserProfile profile = new UserProfile(savedUser);
            userProfileRepository.save(profile);

            // Create default Inbox workspace
            Workspace inbox = new Workspace("Inbox", savedUser, true);
            inbox.setDescription("Quick capture notes");
            inbox.setIcon("📥");
            workspaceRepository.save(inbox);

            // Convert to DTO
            UserDTO userDTO = dtoMapper.toUserDTO(savedUser);

            return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // In a real application, you would invalidate the token here
        // For JWT, you might add the token to a blacklist
        return ResponseEntity.ok("Logout successful");
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String token) {
        try {
            // In a real application, verify the JWT token here
            // For now, just check if token exists and follows our format
            if (token != null && !token.isEmpty() && token.startsWith("token_")) {
                return ResponseEntity.ok("Token is valid");
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token verification failed");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String token) {
        try {
            // In production, decode JWT token to get user ID
            // For now, we'll just validate token format
            if (token == null || token.isEmpty() || !token.startsWith("token_")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
            }

            // This is a placeholder - in production, extract user from JWT
            return ResponseEntity.ok("Get user from token - implement JWT first");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Failed to get current user: " + e.getMessage());
        }
    }
}