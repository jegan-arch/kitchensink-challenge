package com.modern.kitchensink.repository;

import com.modern.kitchensink.model.Member;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends MongoRepository<Member, String> {

    boolean existsByEmail(String email);

    Optional<Member> findByEmail(String email);
}