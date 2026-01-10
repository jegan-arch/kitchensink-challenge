package com.modern.member.repository;

import com.modern.member.model.Member;
import com.modern.member.model.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends MongoRepository<Member, String> {
    Optional<Member> findByUserName(String username);
    Boolean existsByUserName(String username);
    Boolean existsByEmail(String email);
    Boolean existsByPhoneNumber(String phoneNumber);
    List<Member> findByRoleNot(Role role);
    long countByRole(Role role);
}