package com.lookfit.auth.service;

import com.lookfit.auth.dto.AuthDto;
import com.lookfit.global.exception.BusinessException;
import com.lookfit.global.exception.ErrorCode;
import com.lookfit.global.security.JwtTokenProvider;
import com.lookfit.member.domain.Member;
import com.lookfit.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원가입
     */
    @Transactional
    public AuthDto.AuthResponse signup(AuthDto.SignupRequest request) {
        // 이메일 중복 확인
        if (memberRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATE_MEMBER);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 회원 생성
        String memberId = "M" + UUID.randomUUID().toString().substring(0, 8);

        Member member = Member.builder()
                .memberid(memberId)
                .email(request.getEmail())
                .password(encodedPassword)
                .membername(request.getMemberName())
                .phone(request.getPhone())
                .gender(request.getGender())
                .age(request.getAge())
                .grade("USER")
                .regflag("G") // General (일반 회원)
                .enrolldate(LocalDateTime.now())
                .delflag("N")
                .build();

        memberRepository.save(member);

        log.info("New member registered: {}", member.getEmail());

        // JWT 토큰 생성
        String token = jwtTokenProvider.createToken(member.getMemberid(), member.getGrade());

        return AuthDto.AuthResponse.builder()
                .token(token)
                .memberId(member.getMemberid())
                .memberName(member.getMembername())
                .email(member.getEmail())
                .grade(member.getGrade())
                .build();
    }

    /**
     * 로그인
     */
    @Transactional(readOnly = true)
    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        // 이메일로 회원 조회
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        // 탈퇴 회원 확인
        if ("Y".equals(member.getDelflag())) {
            throw new BusinessException(ErrorCode.DELETED_MEMBER);
        }

        log.info("Member logged in: {}", member.getEmail());

        // JWT 토큰 생성
        String token = jwtTokenProvider.createToken(member.getMemberid(), member.getGrade());

        return AuthDto.AuthResponse.builder()
                .token(token)
                .memberId(member.getMemberid())
                .memberName(member.getMembername())
                .email(member.getEmail())
                .grade(member.getGrade())
                .build();
    }
}
