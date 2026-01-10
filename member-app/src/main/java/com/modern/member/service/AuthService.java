package com.modern.member.service;

import com.modern.member.dto.JwtResponse;
import com.modern.member.dto.LoginRequest;
import com.modern.member.dto.MemberResponse;
import com.modern.member.dto.SignupRequest;

public interface AuthService {

    JwtResponse authenticateUser(LoginRequest loginRequest);

    MemberResponse registerUser(SignupRequest signUpRequest);
}