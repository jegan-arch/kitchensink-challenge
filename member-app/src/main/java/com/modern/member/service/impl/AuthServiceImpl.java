package com.modern.member.service.impl;

import com.modern.member.dto.JwtResponse;
import com.modern.member.dto.LoginRequest;
import com.modern.member.dto.MemberResponse;
import com.modern.member.dto.SignupRequest;
import com.modern.member.exception.BusinessException;
import com.modern.member.exception.ErrorCode;
import com.modern.member.model.Member;
import com.modern.member.model.Role;
import com.modern.member.repository.MemberRepository;
import com.modern.member.service.AuthService;
import com.modern.member.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final MemberRepository memberRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    @Override
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.userName(), loginRequest.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String role = Objects.requireNonNull(userDetails).getAuthorities().iterator().next().getAuthority();

        return new JwtResponse(
                jwt,
                "Bearer",
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                role,
                userDetails.isPasswordTemporary()
        );
    }

    @Override
    @Transactional
    public MemberResponse registerUser(SignupRequest signUpRequest) {
        if (memberRepository.existsByEmail(signUpRequest.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_IN_USE);
        }

        if (memberRepository.existsByUserName(signUpRequest.userName())) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        if (memberRepository.existsByPhoneNumber(signUpRequest.phoneNumber())) {
            throw new BusinessException(ErrorCode.PHONE_NUMBER_IN_USE);
        }

        if (!signUpRequest.phoneNumber().matches("^[6-9]\\d{9}$")) {
            throw new BusinessException(ErrorCode.INVALID_PHONE_FORMAT);
        }

        Role role = Role.ROLE_USER;
        if (signUpRequest.role() != null) {
            try {
                role = Role.valueOf(signUpRequest.role().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessException(ErrorCode.ROLE_NOT_FOUND);
            }
        }

        Member member = new Member();
        member.setName(signUpRequest.name());
        member.setEmail(signUpRequest.email());
        member.setPhoneNumber(signUpRequest.phoneNumber());
        member.setUserName(signUpRequest.userName());
        member.setPassword(encoder.encode("Welcome@123"));
        member.setRole(role);
        member.setPasswordTemporary(true);

        Member savedMember = memberRepository.save(member);

        return mapToResponse(savedMember);
    }

    private MemberResponse mapToResponse(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getName(),
                member.getUserName(),
                member.getEmail(),
                member.getPhoneNumber(),
                member.getRole(),
                member.getCreatedAt(),
                member.getUpdatedAt(),
                member.getCreatedBy() == null ? "System" : member.getCreatedBy(),
                member.getUpdatedBy() == null ? "System" : member.getUpdatedBy()
        );
    }
}