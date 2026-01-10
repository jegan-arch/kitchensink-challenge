package com.modern.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modern.member.dto.JwtResponse;
import com.modern.member.dto.LoginRequest;
import com.modern.member.dto.MemberResponse;
import com.modern.member.dto.SignupRequest;
import com.modern.member.model.Role;
import com.modern.member.security.AuthEntryPointJwt;
import com.modern.member.service.AuthService;
import com.modern.member.service.impl.UserDetailsServiceImpl;
import com.modern.member.utils.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class, properties = "spring.main.allow-bean-definition-overriding=true")
@Import(AuthControllerTest.TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean private UserDetailsServiceImpl userDetailsService;
    @MockitoBean private AuthEntryPointJwt authEntryPointJwt;
    @MockitoBean private JwtUtils jwtUtils;

    @TestConfiguration
    @EnableWebSecurity
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth
                            .anyRequest().permitAll()
                    );
            return http.build();
        }
    }

    @Test
    void authenticateUser_Success() throws Exception {
        LoginRequest loginRequest = new LoginRequest("admin", "password123");
        JwtResponse jwtResponse = new JwtResponse(
                "mock-jwt-token", "Bearer", "123", "test", "admin@test.com", "ROLE_ADMIN", false
        );

        when(authService.authenticateUser(any(LoginRequest.class))).thenReturn(jwtResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"));
    }

    @Test
    void authenticateUser_ValidationFailure_Returns400() throws Exception {
        LoginRequest invalidRequest = new LoginRequest("admin", "");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_Success_WhenAdmin() throws Exception {
        SignupRequest signupRequest = new SignupRequest(
                "newuser", "ROLE_USER", "User Name", "user@test.com", "9876543210"
        );

        MemberResponse response = new MemberResponse(
                "1", "newuser", "User Name", "user@test.com", "9876543210",
                Role.ROLE_ADMIN, LocalDateTime.now(), LocalDateTime.now(), "admin", "admin"
        );

        when(authService.registerUser(any(SignupRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/signup")
                        .with(user("admin").password("pass").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));
    }

    @Test
    void registerUser_Forbidden_WhenNotAdmin() throws Exception {
        SignupRequest signupRequest = new SignupRequest(
                "hacker", "ROLE_ADMIN", "Hacker", "hack@test.com", "9876543210"
        );

        mockMvc.perform(post("/api/v1/auth/signup")
                        .with(user("user").password("pass").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isForbidden());
    }
}