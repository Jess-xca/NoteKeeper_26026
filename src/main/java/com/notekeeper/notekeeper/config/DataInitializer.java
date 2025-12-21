package com.notekeeper.notekeeper.config;

import com.notekeeper.notekeeper.model.LocationType;
import com.notekeeper.notekeeper.model.User;
import com.notekeeper.notekeeper.repository.LocationRepository;
import com.notekeeper.notekeeper.repository.UserRepository;
import com.notekeeper.notekeeper.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserService userService, UserRepository userRepository,
            LocationRepository locationRepository,
            org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        return args -> {
            // Drop stale constraint if it exists to allow new NotificationType values
            try {
                jdbcTemplate.execute("ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_type_check");
                System.out.println("✅ Dropped stale notifications_type_check constraint");
            } catch (Exception e) {
                System.out.println("⚠️ Could not drop constraint (might not exist): " + e.getMessage());
            }

            // --- SEED SELECTION ---
            // Jessica Admin
            seedUser(userRepository, userService, locationRepository,
                    "Jessica", "Irakoze", "jessicairakoze4@gmail.com", "jessica_admin",
                    "Admin123!", "ADMIN", "FEMALE", "+250788000001", LocalDate.of(1995, 5, 10));

            // Alain Editor
            seedUser(userRepository, userService, locationRepository,
                    "Alain", "Muvunyi", "bigjess000@gmail.com", "alain_editor",
                    "Editor123!", "EDITOR", "MALE", "+250788000002", LocalDate.of(1996, 8, 20));

            // Divine User
            seedUser(userRepository, userService, locationRepository,
                    "Divine", "Umutoni", "jessicairakoze04@gmail.com", "divine_user",
                    "User123!", "USER", "FEMALE", "+250788000003", LocalDate.of(1997, 12, 15));
        };
    }

    private void seedUser(UserRepository userRepository, UserService userService,
            LocationRepository locationRepository,
            String first, String last, String email, String username,
            String pass, String role, String gender, String phone, LocalDate dob) {

        if (userRepository.findByUsername(username).isPresent() || userRepository.findByEmail(email).isPresent()) {
            System.out.println("⏭️ User already exists, skipping seed: " + username);
            return;
        }

        User user = new User();
        user.setFirstName(first);
        user.setLastName(last);
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(pass);
        user.setRole(role);
        user.setGender(gender);
        user.setPhoneNumber(phone);
        user.setDateOfBirth(dob);
        user.setTwoFactorEnabled(true); // Mandatory 2FA enabled as requested

        // Assign a random Village location if possible
        locationRepository.findAll().stream()
                .filter(l -> l.getType() == LocationType.VILLAGE)
                .findAny()
                .ifPresent(user::setLocation);

        userService.createUser(user);
        System.out.println("✅ Seeded " + role + ": " + first + " " + last + " (" + username + ")");
    }
}
