package com.lookfit.global.security;

import com.lookfit.member.domain.Member;
import com.lookfit.member.repository.MemberRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @SneakyThrows
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Google OAuth2에서 이메일과 이름 가져오기
        String email = (String) oAuth2User.getAttributes().get("email");
        String name = (String) oAuth2User.getAttributes().get("name");

        // 이메일을 memberid로 사용
        String memberId = email;

        // Member가 없으면 생성 (첫 로그인)
        Member member = memberRepository.findById(memberId)
                .orElseGet(() -> {
                    Member newMember = Member.builder()
                            .memberid(memberId)
                            .membername(name != null ? name : "사용자")
                            .email(email)
                            .grade("USER")
                            .regflag("S") // Social 로그인
                            .enrolldate(java.time.LocalDateTime.now())
                            .build();
                    return memberRepository.save(newMember);
                });

        // JWT 토큰 생성
        String token = jwtTokenProvider.createToken(memberId, "USER");

        // 프론트엔드로 리다이렉트 (토큰과 사용자 정보 전달)
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/login/success")
                .queryParam("token", token)
                .queryParam("memberId", memberId)
                .queryParam("memberName", member.getMembername())
                .build().encode().toUriString();  // 한글 인코딩 처리

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}