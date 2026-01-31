package com.lookfit.member.service;

import com.lookfit.member.domain.Member;
import com.lookfit.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    @Autowired
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Optional<Member> findMemberById(String memberId) {
        return memberRepository.findById(memberId);
    }

    public Member saveMember(Member member) {
        return memberRepository.save(member);
    }
}
