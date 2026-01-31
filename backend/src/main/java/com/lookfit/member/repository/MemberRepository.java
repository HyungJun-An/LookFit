package com.lookfit.member.repository;

import java.util.Optional;

import com.lookfit.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {
    Optional<Member> findByEmail(String email);
}