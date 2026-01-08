package com.lookfit.repository;

import com.lookfit.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {
    // 기본 CRUD(save, findById 등)는 자동으로 생성됩니다.
}