package com.modern.member.service.impl;

import com.modern.member.model.Member;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {
    @Serial
    private static final long serialVersionUID = 1L;

    private String id;
    private String username;
    private String email;
    @JsonIgnore
    private String password;
    private boolean isPasswordTemporary;
    private Integer tokenVersion;
    private Collection<? extends GrantedAuthority> authorities;

    public static UserDetailsImpl build(Member member) {
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(member.getRole().name())
        );

        return new UserDetailsImpl(
                member.getId(),
                member.getUserName(),
                member.getEmail(),
                member.getPassword(),
                member.isPasswordTemporary(),
                member.getTokenVersion(),
                authorities
        );
    }
}