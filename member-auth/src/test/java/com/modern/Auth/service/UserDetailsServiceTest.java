package com.modern.Auth.service;

import com.modern.Auth.model.User;
import com.modern.Auth.repository.UserRepository;
import com.modern.Auth.service.impl.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_WhenUserExists_ShouldReturnUserDetails() {
        String username = "testuser";
        User mockUser = new User(username, "test@example.com", "password123");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));

        UserDetails result = userDetailsService.loadUserByUsername(username);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(mockUser.getPassword(), result.getPassword());
        verify(userRepository).findByUsername(username);
    }

    @Test
    void loadUserByUsername_WhenUserNotFound_ShouldThrowException() {
        String username = "unknownUser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(username));

        verify(userRepository).findByUsername(username);
    }
}