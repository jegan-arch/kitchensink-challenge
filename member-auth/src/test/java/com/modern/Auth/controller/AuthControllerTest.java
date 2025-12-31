package com.modern.Auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modern.Auth.dto.JwtResponse;
import com.modern.Auth.dto.LoginRequest;
import com.modern.Auth.dto.SignupRequest;
import com.modern.Auth.exception.ErrorCode;
import com.modern.Auth.exception.UserAlreadyExistsException;
import com.modern.Auth.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Should return 200 OK and JWT Token when login is valid")
    void authenticateUser_WhenValidCredentials_ShouldReturnJwt() throws Exception {
        LoginRequest loginRequest = new LoginRequest("john_doe", "securePass123");

        JwtResponse expectedResponse = new JwtResponse(
                "mock-jwt-token-12345",
                "123",
                "john_doe",
                "john_doe",
                List.of("ROLE_USER")
        );

        given(authService.authenticateUser(any(LoginRequest.class))).willReturn(expectedResponse);

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token-12345"))
                .andExpect(jsonPath("$.username").value("john_doe"));
    }

    @Test
    @DisplayName("Should return 200 OK when signup is successful")
    void registerUser_WhenValidRequest_ShouldReturnSuccessMessage() throws Exception {
        SignupRequest signupRequest = new SignupRequest("new_user", "new@example.com", Set.of("user"), "newPass123");
        doNothing().when(authService).registerUser(any(SignupRequest.class));
        mockMvc.perform(post("/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully!"));

        verify(authService, times(1)).registerUser(any(SignupRequest.class));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when request body is invalid")
    void registerUser_WhenInvalidRequest_ShouldReturn400() throws Exception {
        SignupRequest invalidRequest = new SignupRequest("", "", Set.of(), "");

        mockMvc.perform(post("/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signup_WhenUserAlreadyExists_ShouldReturnError() throws Exception {
        SignupRequest signupRequest = new SignupRequest("testuser", "existing@example.com", Set.of(), "password123");
        Mockito.doThrow(new UserAlreadyExistsException(ErrorCode.USERNAME_ALREADY_TAKEN))
                .when(authService).registerUser(Mockito.any(SignupRequest.class));

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void signup_WhenEmailIsInvalid_ShouldReturnBadRequest() throws Exception {
        SignupRequest invalidRequest = new SignupRequest("testuser", "invalid-email", Set.of(), "password123");

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}