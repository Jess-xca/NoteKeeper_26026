package com.notekeeper.notekeeper.controller;

import com.notekeeper.notekeeper.config.SecurityConfig;
import com.notekeeper.notekeeper.security.CustomUserDetailsService;
import com.notekeeper.notekeeper.security.JwtAuthenticationFilter;
import com.notekeeper.notekeeper.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = ProtectedResourceProbe.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class ProtectedResourceSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    void protectedRouteRejectsMissingToken() throws Exception {
        mockMvc.perform(get("/api/probe"))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedRouteAcceptsValidToken() throws Exception {
        UserDetails user = User.withUsername("ada").password("secret").roles("USER").build();
        when(jwtUtil.extractUsername("good-token")).thenReturn("ada");
        when(jwtUtil.validateToken("good-token", "ada")).thenReturn(true);
        when(userDetailsService.loadUserByUsername("ada")).thenReturn(user);

        mockMvc.perform(get("/api/probe").header("Authorization", "Bearer good-token"))
                .andExpect(status().isOk());
    }
}
