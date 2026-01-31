package com.lookfit.global.security;

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

    @SneakyThrows
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // CustomOAuth2UserService에서 넘겨준 정보를 바탕으로 토큰 생성
        // (실제로는 DB에서 해당 유저의 role을 가져와야 합니다)
        String memberId = (String) oAuth2User.getAttributes().get("sub"); // 또는 생성한 memberid
        String token = jwtTokenProvider.createToken(memberId, "ROLE_USER");
        System.out.println("토큰 생성 완료: " + token);
        // 토큰을 쿼리 스트링으로 전달 (프론트가 로컬 스토리지에 저장하도록)
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/login/success")
                .queryParam("token", token)
                .queryParam("memberId", memberId)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}