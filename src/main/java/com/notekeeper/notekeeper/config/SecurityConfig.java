package com.notekeeper.notekeeper.config;

import com.notekeeper.notekeeper.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users").permitAll()  // Allow user registration
                .requestMatchers(HttpMethod.GET, "/api/locations/**").permitAll()  // Location lookups (sign-up form)
                // Cover images are loaded by <img> tags, which cannot send a token. The controller only
                // serves files inside the uploads folder, and their names are random.
                .requestMatchers(HttpMethod.GET, "/api/attachments/download-by-path").permitAll()
                // System-wide lists and settings are for administrators only
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/locations/**").hasRole("ADMIN")  // editing the location tree
                .requestMatchers(HttpMethod.GET, "/api/profiles/my").authenticated()
                .requestMatchers("/api/profiles/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/users/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET,
                        "/api/users", "/api/users/search", "/api/users/by-location",
                        "/api/users/by-province-code", "/api/users/sorted", "/api/users/paginated").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET,
                        "/api/pages", "/api/pages/paginated", "/api/pages/search",
                        "/api/workspaces", "/api/workspaces/paginated", "/api/workspaces/search",
                        "/api/workspaces/count").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable());
        
        // Needed for H2 console
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));
        
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
