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
import com.notekeeper.notekeeper.exception.BadRequestException;
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
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        // Validate input
        if (loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty()) {
            throw new BadRequestException("Username is required");
        }

        if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
            throw new BadRequestException("Password is required");
        }

        // Find user by username or email
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .or(() -> userRepository.findByEmail(loginRequest.getUsername()))
                .orElseThrow(() -> new BadRequestException("Invalid username or password"));

        // Verify hashed password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            System.out.println("❌ Login failed for user: " + user.getUsername());
            System.out.println("   Input password: " + loginRequest.getPassword());
            System.out.println("   Stored hash: " + user.getPassword());
            System.out.println("   Matches? " + passwordEncoder.matches(loginRequest.getPassword(), user.getPassword()));
            throw new BadRequestException("Invalid username or password");
        }

        // Check if 2FA is enabled - AND FORCE IT TO TRUE per user requirement
        if (user.getTwoFactorEnabled() == null || !user.getTwoFactorEnabled()) {
            System.out.println("⚠️ 2FA forced ENABLED for user: " + user.getUsername());
            user.setTwoFactorEnabled(true);
            userRepository.save(user);
        }
        boolean requiresTwoFactor = true; // Always require it now

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
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody RegisterRequest registerRequest) {
        // Validate required fields
        if (registerRequest.getUsername() == null || registerRequest.getUsername().trim().isEmpty()) {
            throw new BadRequestException("Username is required");
        }

        if (registerRequest.getEmail() == null || registerRequest.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Email is required");
        }

        if (registerRequest.getPassword() == null || registerRequest.getPassword().trim().isEmpty()) {
            throw new BadRequestException("Password is required");
        }

        // Check if username already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BadRequestException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Email already exists");
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
                throw new BadRequestException("Invalid date format. Use YYYY-MM-DD");
            }
        }

        // Set location if provided
        if (registerRequest.getLocationId() != null && !registerRequest.getLocationId().trim().isEmpty()) {
            Location location = locationRepository.findById(registerRequest.getLocationId())
                    .orElseThrow(() -> new BadRequestException("Invalid location ID"));
            user.setLocation(location);
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

        return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.toUserDTO(savedUser));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // In a real application, you would invalidate the token here
        // For JWT, you might add the token to a blacklist
        return ResponseEntity.ok("Logout successful");
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyToken() {
        // If it reaches here, the token is valid due to security filter
        return ResponseEntity.ok("Token is valid");
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@org.springframework.security.core.annotation.AuthenticationPrincipal com.notekeeper.notekeeper.security.UserPrincipal principal) {
        if (principal == null) {
            throw new com.notekeeper.notekeeper.exception.BadRequestException("Not authenticated");
        }
        return ResponseEntity.ok(dtoMapper.toUserDTO(principal.getUser()));
    }

    @PostMapping("/google-login")
    public ResponseEntity<LoginResponse> googleLogin(@RequestBody GoogleLoginRequest googleLoginRequest) {
        // Validate input
        if (googleLoginRequest.getIdToken() == null || googleLoginRequest.getIdToken().trim().isEmpty()) {
            throw new com.notekeeper.notekeeper.exception.BadRequestException("ID token is required");
        }

        // Mock data for testing (In production, use real Google verification)
        String email = "test@example.com";
        String firstName = "Test";
        String lastName = "User";

        // Find or create user
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername(email); 
            newUser.setEmail(email);
            newUser.setPassword(passwordEncoder.encode("dummy_password_" + UUID.randomUUID()));
            newUser.setFirstName(firstName != null ? firstName : "Google");
            newUser.setLastName(lastName != null ? lastName : "User");
            newUser.setRole("USER");
            newUser = userRepository.save(newUser);

            // Create default assets
            UserProfile profile = new UserProfile(newUser);
            userProfileRepository.save(profile);

            Workspace inbox = new Workspace("Inbox", newUser, true);
            inbox.setDescription("Quick capture notes");
            inbox.setIcon("📥");
            workspaceRepository.save(inbox);
            
            return newUser;
        });

        // Handle 2FA for Google Login
        boolean requiresTwoFactor = true; 
        if (user.getTwoFactorEnabled() == null || !user.getTwoFactorEnabled()) {
            user.setTwoFactorEnabled(true);
            userRepository.save(user);
        }

        // Generate and send 2FA code
        twoFactorCodeRepository.deleteByUserId(user.getId());
        TwoFactorCode twoFactorCode = new TwoFactorCode(user);
        twoFactorCodeRepository.save(twoFactorCode);
        
        System.out.println("========================================");
        System.out.println("📧 2FA PIN for Google Login: " + email);
        System.out.println("🔑 PIN CODE: " + twoFactorCode.getCode());
        System.out.println("========================================");
        
        try {
            emailService.send2FACode(email, twoFactorCode.getCode());
        } catch (Exception e) {
            System.out.println("❌ Failed to send 2FA email: " + e.getMessage());
        }

        String token = jwtUtil.generateToken(user.getUsername());
        UserDTO userDTO = dtoMapper.toUserDTO(user);

        LoginResponse response = new LoginResponse(
                true,
                "Check your email for the verification code",
                token,
                userDTO);
        response.setRequiresTwoFactor(true);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/google-callback")
    public ResponseEntity<LoginResponse> googleCallback(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        if (code == null || code.trim().isEmpty()) {
            throw new com.notekeeper.notekeeper.exception.BadRequestException("Authorization code is required");
        }
        
        // This logic is mostly external service calls, keeping it robust
        try {
            // Exchange code and get user info (Keeping existing logic but standardizing response)
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
                throw new com.notekeeper.notekeeper.exception.BadRequestException("Failed to exchange code with Google");
            }
            
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> tokenData = mapper.readValue(tokenResponse.body(), Map.class);
            String accessToken = (String) tokenData.get("access_token");
            
            String userInfoEndpoint = "https://www.googleapis.com/oauth2/v2/userinfo";
            java.net.http.HttpRequest userInfoRequest = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(userInfoEndpoint))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();
            
            java.net.http.HttpResponse<String> userInfoResponse = client.send(userInfoRequest,
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            
            if (userInfoResponse.statusCode() != 200) {
                throw new com.notekeeper.notekeeper.exception.BadRequestException("Failed to get user info from Google");
            }
            
            Map<String, Object> userInfo = mapper.readValue(userInfoResponse.body(), Map.class);
            String email = (String) userInfo.get("email");
            String firstName = (String) userInfo.getOrDefault("given_name", "User");
            String lastName = (String) userInfo.getOrDefault("family_name", "");
            
            // Find or create user
            User user = userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = new User();
                newUser.setUsername(email.split("@")[0]);
                newUser.setEmail(email);
                newUser.setPassword(passwordEncoder.encode("google_oauth_" + UUID.randomUUID()));
                newUser.setFirstName(firstName);
                newUser.setLastName(lastName);
                newUser.setRole("USER");
                newUser = userRepository.save(newUser);
                
                UserProfile profile = new UserProfile(newUser);
                userProfileRepository.save(profile);
                
                Workspace inbox = new Workspace("Inbox", newUser, true);
                inbox.setDescription("Quick capture notes");
                inbox.setIcon("📥");
                workspaceRepository.save(inbox);
                return newUser;
            });
            
            // 2FA enforcement
            if (user.getTwoFactorEnabled() == null || !user.getTwoFactorEnabled()) {
                user.setTwoFactorEnabled(true);
                userRepository.save(user);
            }
            
            twoFactorCodeRepository.deleteByUserId(user.getId());
            TwoFactorCode twoFactorCode = new TwoFactorCode(user);
            twoFactorCodeRepository.save(twoFactorCode);
            
            System.out.println("========================================");
            System.out.println("📧 2FA PIN for Google Login: " + email);
            System.out.println("🔑 PIN CODE: " + twoFactorCode.getCode());
            System.out.println("========================================");
            
            try {
                emailService.send2FACode(email, twoFactorCode.getCode());
            } catch (Exception emailEx) {
                System.out.println("❌ Failed to send 2FA email");
            }
            
            String token = jwtUtil.generateToken(user.getUsername());
            UserDTO userDTO = dtoMapper.toUserDTO(user);
            
            LoginResponse loginResponse = new LoginResponse(
                    true,
                    "Check your email for the verification code",
                    token,
                    userDTO);
            loginResponse.setRequiresTwoFactor(true);
            
            return ResponseEntity.ok(loginResponse);
            
        } catch (Exception e) {
            if (e instanceof com.notekeeper.notekeeper.exception.BadRequestException) throw (com.notekeeper.notekeeper.exception.BadRequestException)e;
            throw new RuntimeException("Google callback failed: " + e.getMessage());
        }
    }
    
    @PostMapping("/verify-2fa")
    public ResponseEntity<LoginResponse> verify2FA(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String code = request.get("code");
        
        if (userId == null || code == null) {
            throw new com.notekeeper.notekeeper.exception.BadRequestException("User ID and code are required");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.notekeeper.notekeeper.exception.ResourceNotFoundException("User not found"));
        
        List<TwoFactorCode> codes = twoFactorCodeRepository.findByUserIdOrderByExpiryDateDesc(userId);
        if (codes.isEmpty()) {
            throw new com.notekeeper.notekeeper.exception.BadRequestException("No verification code found");
        }
        
        TwoFactorCode latestCode = codes.get(0);
        
        if (!latestCode.getCode().equals(code)) {
            throw new com.notekeeper.notekeeper.exception.BadRequestException("Invalid verification code");
        }
        
        if (latestCode.isExpired()) {
            throw new com.notekeeper.notekeeper.exception.BadRequestException("Verification code has expired");
        }
        
        if (latestCode.isUsed()) {
            throw new com.notekeeper.notekeeper.exception.BadRequestException("Verification code already used");
        }
        
        latestCode.setUsed(true);
        twoFactorCodeRepository.save(latestCode);
        
        String token = jwtUtil.generateToken(user.getUsername());
        UserDTO userDTO = dtoMapper.toUserDTO(user);
        
        LoginResponse response = new LoginResponse(true, "Login successful", token, userDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            throw new com.notekeeper.notekeeper.exception.BadRequestException("Email is required");
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            passwordResetTokenRepository.deleteByUserId(user.getId());
            PasswordResetToken resetToken = new PasswordResetToken(user);
            passwordResetTokenRepository.save(resetToken);

            System.out.println("🔑 PASSWORD RESET TOKEN: " + resetToken.getToken());
            try {
                emailService.sendPasswordResetEmail(user.getEmail(), resetToken.getToken());
            } catch (Exception e) {
                System.out.println("❌ Failed to send reset email");
            }
        }

        // Always return success for security
        return ResponseEntity.ok(Map.of("message", "If an account exists with this email, a reset link has been sent"));
    }

    @GetMapping("/verify-reset-token")
    public ResponseEntity<Map<String, Boolean>> verifyResetToken(@RequestParam String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new com.notekeeper.notekeeper.exception.BadRequestException("Token is required");
        }

        Optional<PasswordResetToken> resetTokenOpt = passwordResetTokenRepository.findByToken(token);
        
        if (resetTokenOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("valid", false));
        }

        PasswordResetToken resetToken = resetTokenOpt.get();
        if (resetToken.isExpired() || resetToken.isUsed()) {
            return ResponseEntity.ok(Map.of("valid", false));
        }

        return ResponseEntity.ok(Map.of("valid", true));
    }

    @PostMapping("/reset-password")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        if (token == null || newPassword == null || newPassword.trim().isEmpty()) {
            throw new com.notekeeper.notekeeper.exception.BadRequestException("Token and new password are required");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new com.notekeeper.notekeeper.exception.BadRequestException("Invalid or expired reset token"));

        if (resetToken.isExpired() || resetToken.isUsed()) {
            throw new com.notekeeper.notekeeper.exception.BadRequestException("Invalid or expired reset token");
        }

        // Fetch user explicitly to ensure we are updating the actual record in the DB
        User user = userRepository.findById(resetToken.getUser().getId())
                .orElseThrow(() -> new com.notekeeper.notekeeper.exception.ResourceNotFoundException("User not found"));
        
        System.out.println("🔄 Resetting password for user: " + user.getUsername());
        
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
        
        System.out.println("✅ Password reset successfully. New hash: " + encodedPassword);

        return ResponseEntity.ok(Map.of("message", "Password reset successful"));
    }

    @PostMapping("/send-2fa")
    public ResponseEntity<Map<String, String>> send2FACode(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        if (userId == null) {
            throw new com.notekeeper.notekeeper.exception.BadRequestException("User ID is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.notekeeper.notekeeper.exception.ResourceNotFoundException("User not found"));

        twoFactorCodeRepository.deleteByUserId(userId);
        TwoFactorCode code = new TwoFactorCode(user);
        twoFactorCodeRepository.save(code);

        try {
            emailService.send2FACode(user.getEmail(), code.getCode());
        } catch (Exception e) {
            throw new RuntimeException("Failed to send 2FA code: " + e.getMessage());
        }

        return ResponseEntity.ok(Map.of("message", "2FA code sent to your email"));
    }
}
