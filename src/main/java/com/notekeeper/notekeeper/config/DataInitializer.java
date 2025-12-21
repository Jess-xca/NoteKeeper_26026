package com.notekeeper.notekeeper.config;

import com.notekeeper.notekeeper.model.User;
import com.notekeeper.notekeeper.repository.UserRepository;
import com.notekeeper.notekeeper.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserService userService, UserRepository userRepository, org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        return args -> {
            // Drop stale constraint if it exists to allow new NotificationType values
            try {
                jdbcTemplate.execute("ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_type_check");
                System.out.println("✅ Dropped stale notifications_type_check constraint");
            } catch (Exception e) {
                System.out.println("⚠️ Could not drop constraint (might not exist): " + e.getMessage());
            }

            // Seed Admin
            if (!userRepository.existsByEmail("jessica.irakoze@gmail.com")) {
                User admin = new User();
                admin.setFirstName("Jessica");
                admin.setLastName("Irakoze");
                admin.setEmail("jessica.irakoze@gmail.com");
                admin.setUsername("jessica_admin");
                admin.setPassword("Admin123!");
                admin.setRole("ADMIN");
                userService.createUser(admin);
                System.out.println("✅ Seeded Admin: Jessica Irakoze");
            }

            // Seed Editor
            if (!userRepository.existsByEmail("alain.muvunyi@gmail.com")) {
                User editor = new User();
                editor.setFirstName("Alain");
                editor.setLastName("Muvunyi");
                editor.setEmail("alain.muvunyi@gmail.com");
                editor.setUsername("alain_editor");
                editor.setPassword("Editor123!");
                editor.setRole("EDITOR");
                userService.createUser(editor);
                System.out.println("✅ Seeded Editor: Alain Muvunyi");
            }

            // Seed User
            if (!userRepository.existsByEmail("divine.umutoni@gmail.com")) {
                User user = new User();
                user.setFirstName("Divine");
                user.setLastName("Umutoni");
                user.setEmail("divine.umutoni@gmail.com");
                user.setUsername("divine_user");
                user.setPassword("User123!");
                user.setRole("USER");
                userService.createUser(user);
                System.out.println("✅ Seeded User: Divine Umutoni");
            }
        };
    }
}
