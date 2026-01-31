package com.lookfit.member.controller;

import com.lookfit.member.service.MemberService;
import com.lookfit.member.domain.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<Member> getMemberById(@PathVariable String memberId) {
        Optional<Member> member = memberService.findMemberById(memberId);
        return member.map(ResponseEntity::ok)
                     .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Member> createMember(@RequestBody Member member) {
        Member savedMember = memberService.saveMember(member);
        return ResponseEntity.ok(savedMember);
    }
}
