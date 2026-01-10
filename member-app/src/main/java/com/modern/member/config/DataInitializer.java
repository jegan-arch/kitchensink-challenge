package com.modern.member.config;

import com.modern.member.model.Member;
import com.modern.member.model.Role;
import com.modern.member.repository.MemberRepository;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(@Nonnull String... args) {
        if (memberRepository.count() == 0) {
            log.info("No members found. Initializing Super Admin account...");

            Member admin = Member.builder()
                    .userName("superadmin")
                    .password(passwordEncoder.encode("admin123"))
                    .name("System Administrator")
                    .email("admin@member.com")
                    .phoneNumber("9999999999")
                    .role(Role.ROLE_ADMIN)
                    .tokenVersion(1)
                    .isPasswordTemporary(false)
                    .build();

            memberRepository.save(admin);
            log.info("Super Admin initialized successfully.");
        }
    }
}