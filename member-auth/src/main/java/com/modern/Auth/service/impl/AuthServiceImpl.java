package com.modern.Auth.service.impl;

import com.modern.Auth.dto.JwtResponse;
import com.modern.Auth.dto.LoginRequest;
import com.modern.Auth.dto.SignupRequest;
import com.modern.Auth.exception.ErrorCode;
import com.modern.Auth.exception.UserAlreadyExistsException;
import com.modern.Auth.model.Role;
import com.modern.Auth.model.User;
import com.modern.Auth.repository.UserRepository;
import com.modern.Auth.security.utils.JwtUtils;
import com.modern.Auth.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    public AuthServiceImpl(AuthenticationManager authenticationManager, UserRepository userRepository, PasswordEncoder encoder, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = performAuthentication(loginRequest);
        String jwt = jwtUtils.generateJwtToken(authentication);
        return buildJwtResponse(authentication, jwt);
    }

    @Override
    public void registerUser(SignupRequest signUpRequest) {
        validateNewUser(signUpRequest);
        User user = createUserEntity(signUpRequest);
        userRepository.save(user);
    }

    private Authentication performAuthentication(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }

    private JwtResponse buildJwtResponse(Authentication authentication, String jwt) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = Objects.requireNonNull(userDetails).getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles);
    }

    private void validateNewUser(SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.username())) {
            throw new UserAlreadyExistsException(ErrorCode.USERNAME_ALREADY_TAKEN);
        }
        if (userRepository.existsByEmail(signUpRequest.email())) {
            throw new UserAlreadyExistsException(ErrorCode.EMAIL_ALREADY_IN_USE);
        }
    }

    private User createUserEntity(SignupRequest signUpRequest) {
        User user = new User(signUpRequest.username(),
                signUpRequest.email(),
                encoder.encode(signUpRequest.password()));

        user.setRoles(resolveRoles(signUpRequest.roles()));
        return user;
    }

    private Set<String> resolveRoles(Set<String> strRoles) {
        Set<String> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            roles.add(Role.ROLE_USER.name());
            return roles;
        }

        strRoles.forEach(role -> {
            if ("admin".equalsIgnoreCase(role)) {
                roles.add(Role.ROLE_ADMIN.name());
            } else {
                roles.add(Role.ROLE_USER.name());
            }
        });

        return roles;
    }
}