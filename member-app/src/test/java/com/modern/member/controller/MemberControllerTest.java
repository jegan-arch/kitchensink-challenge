package com.modern.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modern.member.dto.ChangePasswordRequest;
import com.modern.member.dto.MemberRequest;
import com.modern.member.dto.MemberResponse;
import com.modern.member.model.Role;
import com.modern.member.security.AuthEntryPointJwt;
import com.modern.member.service.MemberService;
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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MemberController.class, properties = "spring.main.allow-bean-definition-overriding=true")
@Import(MemberControllerTest.TestSecurityConfig.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean private UserDetailsServiceImpl userDetailsService;
    @MockitoBean private AuthEntryPointJwt authEntryPointJwt;
    @MockitoBean private JwtUtils jwtUtils;

    @TestConfiguration
    @EnableWebSecurity
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    @Test
    void getAllMembers_Success_WhenAdmin() throws Exception {
        MemberResponse member = new MemberResponse(
                "1", "User One", "user1", "user1@test.com", "1234567890",
                Role.ROLE_USER, LocalDateTime.now(), LocalDateTime.now(), "admin", "admin"
        );
        when(memberService.getAllMembers()).thenReturn(List.of(member));

        mockMvc.perform(get("/api/v1/members")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userName").value("user1"));
    }

    @Test
    void getAllMembers_Forbidden_WhenUser() throws Exception {
        mockMvc.perform(get("/api/v1/members")
                        .with(user("user").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMemberById_Success() throws Exception {
        MemberResponse member = new MemberResponse(
                "1", "user1", "User One", "user1@test.com", "1234567890",
                Role.ROLE_USER, LocalDateTime.now(), LocalDateTime.now(), "admin", "admin"
        );
        when(memberService.getMemberById("1")).thenReturn(member);

        mockMvc.perform(get("/api/v1/members/1")
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));
    }

    @Test
    void getMyProfile_Success() throws Exception {
        MemberResponse member = new MemberResponse(
                "1", "My Self", "me", "me@test.com", "1234567890",
                Role.ROLE_USER, LocalDateTime.now(), LocalDateTime.now(), "admin", "admin"
        );
        when(memberService.getMyProfile()).thenReturn(member);

        mockMvc.perform(get("/api/v1/members/me")
                        .with(user("me").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("me"));
    }

    @Test
    void updateProfile_Success() throws Exception {
        MemberRequest request = new MemberRequest("Updated Name", "user1@test.com", "1234567890", "ROLE_USER");
        MemberResponse response = new MemberResponse(
                "1", "Updated Name", "user1", "user1@test.com", "1234567890",
                Role.ROLE_USER, LocalDateTime.now(), LocalDateTime.now(), "admin", "admin"
        );

        when(memberService.updateMember(eq("1"), any(MemberRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/members/1")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void deleteMember_Success_WhenAdmin() throws Exception {
        doNothing().when(memberService).deleteMember("1");

        mockMvc.perform(delete("/api/v1/members/1")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Member deleted successfully."));
    }

    @Test
    void deleteMember_Forbidden_WhenUser() throws Exception {
        mockMvc.perform(delete("/api/v1/members/1")
                        .with(user("user").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUserRole_Success_WhenAdmin() throws Exception {
        doNothing().when(memberService).updateMemberRole("1", "ROLE_ADMIN");

        mockMvc.perform(put("/api/v1/members/1/role")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role", "ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Role updated and session invalidated."));
    }

    @Test
    void updateUserRole_Forbidden_WhenUser() throws Exception {
        mockMvc.perform(put("/api/v1/members/1/role")
                        .with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role", "ROLE_ADMIN"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void changePassword_Success() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("oldPass", "newPass");
        doNothing().when(memberService).changePassword(eq("1"), any(ChangePasswordRequest.class));

        mockMvc.perform(put("/api/v1/members/1/change-password")
                        .with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully."));
    }
}