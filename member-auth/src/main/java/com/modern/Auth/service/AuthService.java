package com.modern.Auth.service;

import com.modern.Auth.dto.JwtResponse;
import com.modern.Auth.dto.LoginRequest;
import com.modern.Auth.dto.SignupRequest;

public interface AuthService {

    JwtResponse authenticateUser(LoginRequest loginRequest);

    void registerUser(SignupRequest signUpRequest);
}