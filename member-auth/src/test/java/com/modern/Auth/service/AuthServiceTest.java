package com.modern.Auth.service;

import com.modern.Auth.dto.JwtResponse;
import com.modern.Auth.dto.LoginRequest;
import com.modern.Auth.dto.SignupRequest;
import com.modern.Auth.exception.ErrorCode;
import com.modern.Auth.exception.UserAlreadyExistsException;
import com.modern.Auth.model.Role;
import com.modern.Auth.model.User;
import com.modern.Auth.repository.UserRepository;
import com.modern.Auth.security.utils.JwtUtils;
import com.modern.Auth.service.impl.AuthServiceImpl;
import com.modern.Auth.service.impl.UserDetailsImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private Authentication authentication;

    @Test
    void authenticateUser_WhenCredentialsValid_ShouldReturnJwtResponse() {
        LoginRequest loginRequest = new LoginRequest("testuser", "password");
        String expectedToken = "jwt-token-xyz";
        UserDetailsImpl userDetails = new UserDetailsImpl(
                "123",
                "testuser",
                "test@example.com",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn(expectedToken);

        JwtResponse response = authService.authenticateUser(loginRequest);

        assertNotNull(response);
        assertEquals(expectedToken, response.token());
        assertEquals("testuser", response.username());
        assertEquals("test@example.com", response.email());
        assertTrue(response.roles().contains("ROLE_USER"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void registerUser_WhenNewUser_ShouldSaveUserWithEncodedPassword() {
        SignupRequest signupRequest = new SignupRequest(
                "newuser",
                "new@example.com",
                Set.of("user"), // Roles
                "plainPassword"
        );

        when(userRepository.existsByUsername(signupRequest.username())).thenReturn(false);
        when(userRepository.existsByEmail(signupRequest.email())).thenReturn(false);
        when(encoder.encode(signupRequest.password())).thenReturn("encodedPassword");

        authService.registerUser(signupRequest);

        verify(userRepository).save(argThat(user ->
                user.getUsername().equals("newuser") &&
                        user.getEmail().equals("new@example.com") &&
                        user.getPassword().equals("encodedPassword") &&
                        user.getRoles().contains(Role.ROLE_USER.name())
        ));
    }

    @Test
    void registerUser_WhenAdminRoleRequested_ShouldSaveAdminRole() {
        SignupRequest signupRequest = new SignupRequest(
                "adminUser",
                "admin@example.com",
                Set.of("admin"),
                "password"
        );

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(encoder.encode(anyString())).thenReturn("encoded");

        authService.registerUser(signupRequest);

        verify(userRepository).save(argThat(user ->
                user.getRoles().contains(Role.ROLE_ADMIN.name())
        ));
    }

    @Test
    void registerUser_WhenUsernameExists_ShouldThrowException() {
        SignupRequest signupRequest = new SignupRequest("takenUser", "email@test.com", null, "pass");
        when(userRepository.existsByUsername("takenUser")).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> authService.registerUser(signupRequest)
        );

        assertEquals(ErrorCode.USERNAME_ALREADY_TAKEN, exception.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_WhenEmailExists_ShouldThrowException() {
        SignupRequest signupRequest = new SignupRequest("user", "taken@test.com", null, "pass");
        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> authService.registerUser(signupRequest)
        );

        assertEquals(ErrorCode.EMAIL_ALREADY_IN_USE, exception.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }
}