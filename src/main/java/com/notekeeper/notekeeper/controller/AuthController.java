package com.notekeeper.notekeeper.controller;

import com.notekeeper.notekeeper.dto.*;
import com.notekeeper.notekeeper.mapper.DTOMapper;
import com.notekeeper.notekeeper.model.Location;
import com.notekeeper.notekeeper.model.User;
import com.notekeeper.notekeeper.model.UserProfile;
import com.notekeeper.notekeeper.model.Workspace;
import com.notekeeper.notekeeper.repository.UserRepository;
import com.notekeeper.notekeeper.repository.UserProfileRepository;
import com.notekeeper.notekeeper.repository.WorkspaceRepository;
import com.notekeeper.notekeeper.repository.PasswordResetTokenRepository;
import com.notekeeper.notekeeper.repository.TwoFactorCodeRepository;
import com.notekeeper.notekeeper.service.EmailService;
import com.notekeeper.notekeeper.model.PasswordResetToken;
import com.notekeeper.notekeeper.model.TwoFactorCode;
import com.notekeeper.notekeeper.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.notekeeper.notekeeper.repository.LocationRepository locationRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private DTOMapper dtoMapper;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private TwoFactorCodeRepository twoFactorCodeRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Value("${google.client.id}")
    private String googleClientId;
    
    @Value("${google.client.secret:}")
    private String googleClientSecret;

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

            // Verify hashed password
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new LoginResponse(false, "Invalid username or password", null, null));
            }

            // Check if 2FA is enabled
            boolean requiresTwoFactor = user.getTwoFactorEnabled() != null && user.getTwoFactorEnabled();

            if (requiresTwoFactor) {
                // Delete old codes for this user
                twoFactorCodeRepository.deleteByUserId(user.getId());
                
                // Generate and send 2FA code
                TwoFactorCode code = new TwoFactorCode(user);
                twoFactorCodeRepository.save(code);
                
                System.out.println("========================================");
                System.out.println("📧 2FA PIN for " + user.getEmail());
                System.out.println("🔑 PIN CODE: " + code.getCode());
                System.out.println("========================================");
                
                try {
                    emailService.send2FACode(user.getEmail(), code.getCode());
                } catch (Exception e) {
                    System.out.println("❌ Failed to send 2FA email: " + e.getMessage());
                }
            }

            // Generate a real JWT token
            String token = jwtUtil.generateToken(user.getUsername());

            // Convert to DTO
            UserDTO userDTO = dtoMapper.toUserDTO(user);

            // Return success response with 2FA flag
            LoginResponse response = new LoginResponse(
                    true,
                    requiresTwoFactor ? "2FA code sent to your email" : "Login successful",
                    token,
                    userDTO);
            
            response.setRequiresTwoFactor(requiresTwoFactor);

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
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
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
            // Verify JWT token
            if (token != null && token.startsWith("Bearer ")) {
                String jwt = token.substring(7);
                String username = jwtUtil.extractUsername(jwt);
                if (username != null && !jwtUtil.isTokenExpired(jwt)) {
                    return ResponseEntity.ok("Token is valid");
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token verification failed");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
            }

            String jwt = token.substring(7);
            String username = jwtUtil.extractUsername(jwt);
            
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (!userOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }

            return ResponseEntity.ok(dtoMapper.toUserDTO(userOpt.get()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Failed to get current user: " + e.getMessage());
        }
    }

    @PostMapping("/google-login")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginRequest googleLoginRequest) {
        try {
            // Validate input
            if (googleLoginRequest.getIdToken() == null || googleLoginRequest.getIdToken().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new LoginResponse(false, "ID token is required", null, null));
            }

            // TEMPORARY: Skip Google verification for testing - use mock data
            // In production, uncomment the verification code below
            /*
             * GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new
             * NetHttpTransport(),
             * new GsonFactory())
             * .setAudience(Collections.singletonList(googleClientId))
             * .build();
             * 
             * GoogleIdToken idToken = verifier.verify(googleLoginRequest.getIdToken());
             * if (idToken == null) {
             * return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
             * .body(new LoginResponse(false, "Invalid ID token", null, null));
             * }
             * 
             * GoogleIdToken.Payload payload = idToken.getPayload();
             * String email = payload.getEmail();
             * String firstName = (String) payload.get("given_name");
             * String lastName = (String) payload.get("family_name");
             */

            // Mock data for testing
            String email = "test@example.com";
            String firstName = "Test";
            String lastName = "User";

            // Check if user exists by email
            Optional<User> userOpt = userRepository.findByEmail(email);

            User user;
            if (userOpt.isPresent()) {
                user = userOpt.get();
            } else {
                // Create new user
                user = new User();
                user.setUsername(email); // Use email as username
                user.setEmail(email);
                user.setPassword("dummy_password"); // Dummy password for Google users
                user.setFirstName(firstName != null ? firstName : "Google");
                user.setLastName(lastName != null ? lastName : "User");
                user.setRole("USER");

                user = userRepository.save(user);

                // Create default profile
                UserProfile profile = new UserProfile(user);
                userProfileRepository.save(profile);

                // Create default Inbox workspace
                Workspace inbox = new Workspace("Inbox", user, true);
                inbox.setDescription("Quick capture notes");
                inbox.setIcon("📥");
                workspaceRepository.save(inbox);
            }

            // ✅ AUTOMATICALLY ENABLE 2FA FOR ALL GOOGLE LOGINS
            System.out.println("💡 About to check 2FA status for user: " + email);
            System.out.println("💡 User 2FA enabled: " + user.getTwoFactorEnabled());
            
            boolean requiresTwoFactor = false;
            if (!user.getTwoFactorEnabled()) {
                System.out.println("💡 Enabling 2FA for new user...");
                user.setTwoFactorEnabled(true);
                userRepository.save(user);
                requiresTwoFactor = true;
            } else {
                System.out.println("💡 2FA already enabled for user");
                requiresTwoFactor = true;
            }
            
            System.out.println("💡 requiresTwoFactor = " + requiresTwoFactor);
            
            // Generate and send 2FA code if enabled
            if (requiresTwoFactor) {
                TwoFactorCode twoFactorCode = new TwoFactorCode(user);
                twoFactorCodeRepository.save(twoFactorCode);
                
                System.out.println("========================================");
                System.out.println("📧 2FA PIN for " + email);
                System.out.println("🔑 PIN CODE: " + twoFactorCode.getCode());
                System.out.println("========================================");
                
                try {
                    emailService.send2FACode(email, twoFactorCode.getCode());
                    System.out.println("✅ 2FA email sent successfully to: " + email);
                } catch (Exception emailEx) {
                    System.out.println("❌ Failed to send 2FA email: " + emailEx.getMessage());
                    // Continue even if email fails (PIN is printed to console)
                }
            }
            
            // Generate a simple token
            String token = "token_" + UUID.randomUUID().toString();

            // Convert to DTO
            UserDTO userDTO = dtoMapper.toUserDTO(user);

            // Return success response with 2FA flag
            LoginResponse response = new LoginResponse(
                    true,
                    requiresTwoFactor ? "2FA code sent to your email" : "Google login successful",
                    token,
                    userDTO);
            
            // Add requiresTwoFactor flag to response
            response.setRequiresTwoFactor(requiresTwoFactor);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LoginResponse(false, "Google login failed: " + e.getMessage(), null, null));
        }
    }

    // Google OAuth callback endpoint - called by frontend after Google redirects
    @PostMapping("/google-callback")
    public ResponseEntity<?> googleCallback(@RequestBody Map<String, String> request) {
        System.out.println("\n🔵🔵🔵 GOOGLE CALLBACK ENDPOINT HIT! 🔵🔵🔵");
        
        try {
            String code = request.get("code");
            if (code == null || code.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Authorization code is required"));
            }
            
            String email = null;
            String firstName = null;
            String lastName = null;
            
            try {
                // Exchange code for access token and get user info from Google
                String tokenEndpoint = "https://oauth2.googleapis.com/token";
                java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                
                String requestBody = "code=" + code +
                        "&client_id=" + googleClientId +
                        "&client_secret=" + googleClientSecret +
                        "&redirect_uri=http://localhost:3000/auth/google/callback" +
                        "&grant_type=authorization_code";
                
                java.net.http.HttpRequest tokenRequest = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(tokenEndpoint))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(java.net.http.HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
                
                java.net.http.HttpResponse<String> tokenResponse = client.send(tokenRequest, 
                        java.net.http.HttpResponse.BodyHandlers.ofString());
                
                if (tokenResponse.statusCode() != 200) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("success", false, "message", "Failed to exchange code with Google"));
                }
                
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Map<String, Object> tokenData = mapper.readValue(tokenResponse.body(), Map.class);
                String accessToken = (String) tokenData.get("access_token");
                
                // Get user info from Google
                String userInfoEndpoint = "https://www.googleapis.com/oauth2/v2/userinfo";
                java.net.http.HttpRequest userInfoRequest = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(userInfoEndpoint))
                        .header("Authorization", "Bearer " + accessToken)
                        .GET()
                        .build();
                
                java.net.http.HttpResponse<String> userInfoResponse = client.send(userInfoRequest,
                        java.net.http.HttpResponse.BodyHandlers.ofString());
                
                if (userInfoResponse.statusCode() != 200) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("success", false, "message", "Failed to get user info from Google"));
                }
                
                Map<String, Object> userInfo = mapper.readValue(userInfoResponse.body(), Map.class);
                email = (String) userInfo.get("email");
                firstName = (String) userInfo.getOrDefault("given_name", "User");
                lastName = (String) userInfo.getOrDefault("family_name", "");
                
            } catch (Exception ex) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("success", false, "message", "Failed to authenticate with Google: " + ex.getMessage()));
            }
            
            // Find or create user
            User user;
            synchronized (this) {
                Optional<User> userOpt = userRepository.findByEmail(email);
                if (userOpt.isPresent()) {
                    user = userOpt.get();
                } else {
                    user = new User();
                    user.setUsername(email.split("@")[0]);
                    user.setEmail(email);
                    user.setPassword(passwordEncoder.encode("google_oauth_user_" + UUID.randomUUID()));
                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    user.setRole("USER");
                    user = userRepository.save(user);
                    
                    UserProfile profile = new UserProfile(user);
                    userProfileRepository.save(profile);
                    
                    Workspace inbox = new Workspace("Inbox", user, true);
                    inbox.setDescription("Quick capture notes");
                    inbox.setIcon("📥");
                    workspaceRepository.save(inbox);
                }
            }
            
            // Handle 2FA for Google Login
            boolean requiresTwoFactor = true; // Always require 2FA for Google if we want it enabled by default
            if (user.getTwoFactorEnabled() == null || !user.getTwoFactorEnabled()) {
                user.setTwoFactorEnabled(true);
                userRepository.save(user);
            }
            
            TwoFactorCode twoFactorCode = new TwoFactorCode(user);
            twoFactorCodeRepository.save(twoFactorCode);
            
            System.out.println("========================================");
            System.out.println("📧 2FA PIN for Google Login: " + email);
            System.out.println("🔑 PIN CODE: " + twoFactorCode.getCode());
            System.out.println("========================================");
            
            try {
                emailService.send2FACode(email, twoFactorCode.getCode());
            } catch (Exception emailEx) {
                System.out.println("❌ Failed to send 2FA email, but continuing...");
            }
            
            String token = jwtUtil.generateToken(user.getUsername());
            UserDTO userDTO = dtoMapper.toUserDTO(user);
            
            LoginResponse loginResponse = new LoginResponse(
                    true,
                    "Check your email for the PIN",
                    token,
                    userDTO);
            loginResponse.setRequiresTwoFactor(true);
            
            return ResponseEntity.ok(loginResponse);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Google callback failed: " + e.getMessage()));
        }
    }
    
    // Verify 2FA PIN code
    @PostMapping("/verify-2fa")
    public ResponseEntity<?> verify2FA(@RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            String code = request.get("code");
            
            System.out.println("🔐 2FA Verification attempt for user: " + userId + " with code: " + code);
            
            if (userId == null || code == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "User ID and code are required"));
            }
            
            // Find user
            Optional<User> userOpt = userRepository.findById(userId);
            if (!userOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "User not found"));
            }
            
            User user = userOpt.get();
            
            // Find the most recent 2FA code for this user
            List<TwoFactorCode> codes = 
                twoFactorCodeRepository.findByUserIdOrderByExpiryDateDesc(userId);
            
            if (codes.isEmpty()) {
                System.out.println("❌ No 2FA code found for user");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "No verification code found"));
            }
            
            TwoFactorCode latestCode = codes.get(0);
            System.out.println("✅ Found 2FA code: " + latestCode.getCode() + " (expected: " + code + ")");
            
            // Check if code matches
            if (!latestCode.getCode().equals(code)) {
                System.out.println("❌ Code mismatch!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Invalid code"));
            }
            
            // Check if code is expired (5 minutes)
            if (latestCode.isExpired()) {
                System.out.println("❌ Code expired!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Code has expired"));
            }
            
            // Check if code was already used
            if (latestCode.isUsed()) {
                System.out.println("❌ Code already used!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Code already used"));
            }
            
            // Mark code as used
            latestCode.setUsed(true);
            twoFactorCodeRepository.save(latestCode);
            System.out.println("✅ Code verified and marked as used");
            
            // Generate real JWT token
            String token = jwtUtil.generateToken(user.getUsername());
            
            // Convert to DTO
            UserDTO userDTO = dtoMapper.toUserDTO(user);
            
            // Return success with token
            LoginResponse response = new LoginResponse(
                    true,
                    "Login successful!",
                    token,
                    userDTO);
            
            System.out.println("✅ 2FA verification successful!");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("❌ Error in verify-2fa: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Verification failed: " + e.getMessage()));
        }
    }

    // ========== PASSWORD RESET ENDPOINTS ==========

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");

            // Find user by email
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (!userOpt.isPresent()) {
                // Return success even if user not found (security best practice)
                return ResponseEntity.ok("If email exists, reset link will be sent");
            }

            User user = userOpt.get();

            // Delete old tokens for this user
            passwordResetTokenRepository.deleteByUserId(user.getId());

            // Create new reset token
            PasswordResetToken resetToken = new PasswordResetToken(user);
            passwordResetTokenRepository.save(resetToken);

            // Send reset email
            System.out.println("🔑 PASSWORD RESET TOKEN: " + resetToken.getToken());
            emailService.sendPasswordResetEmail(user.getEmail(), resetToken.getToken());

            return ResponseEntity.ok("If email exists, reset link will be sent");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process request: " + e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            String newPassword = request.get("newPassword");

            // Find token
            Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(token);
            if (!tokenOpt.isPresent()) {
                return ResponseEntity.badRequest().body("Invalid or expired reset token");
            }

            PasswordResetToken resetToken = tokenOpt.get();

            // Check if expired or already used
            if (resetToken.isExpired() || resetToken.isUsed()) {
                return ResponseEntity.badRequest().body("Invalid or expired reset token");
            }

            // Update user password
            User user = resetToken.getUser();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            // Mark token as used
            resetToken.setUsed(true);
            passwordResetTokenRepository.save(resetToken);

            return ResponseEntity.ok("Password reset successful");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to reset password: " + e.getMessage());
        }
    }

    // ========== TWO-FACTOR AUTHENTICATION ENDPOINTS ==========

    @PostMapping("/send-2fa")
    public ResponseEntity<?> send2FACode(@RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");

            // Get user
            Optional<User> userOpt = userRepository.findById(userId);
            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest().body("User not found");
            }

            User user = userOpt.get();

            // Delete old codes for this user
            twoFactorCodeRepository.deleteByUserId(userId);

            // Create new 2FA code
            TwoFactorCode code = new TwoFactorCode(user);
            twoFactorCodeRepository.save(code);

            // Send code via email
            emailService.send2FACode(user.getEmail(), code.getCode());

            return ResponseEntity.ok("2FA code sent to email");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send 2FA code: " + e.getMessage());
        }
    }
}
