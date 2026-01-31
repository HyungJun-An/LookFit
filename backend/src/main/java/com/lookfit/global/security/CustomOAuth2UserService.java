package com.lookfit.global.security;

import com.lookfit.member.domain.Member;
import com.lookfit.member.domain.SocialAccount;
import com.lookfit.member.repository.MemberRepository;
import com.lookfit.member.repository.SocialAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final SocialAccountRepository socialAccountRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 기본 설정을 통해 구글로부터 유저 정보를 가져옴
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. 제공자 정보(google)와 구글의 고유 식별값(sub) 추출
        String provider = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String providerUserId = (String) attributes.get("sub");
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        // 3. 이미 가입된 소셜 계정인지 확인
        Optional<SocialAccount> socialOpt = socialAccountRepository.findByProviderAndProviderUserId(provider, providerUserId);

        if (socialOpt.isEmpty()) {
            // [신규 가입 로직]
            // 4. Member 아이디 생성 (중복 방지를 위해 provider_고유ID 조합)
            String memberId = provider + "_" + providerUserId.substring(0, 5);

            // 5. Member 테이블 저장
            Member member = Member.builder()
                    .memberid(memberId)
                    .membername(name)
                    .email(email)
                    .regflag("S") // Social
                    .grade("USER")
                    .build();
            memberRepository.save(member);

            // 6. SocialAccount 테이블 저장 (Member와 연관관계 맺기)
            SocialAccount socialAccount = SocialAccount.builder()
                    .member(member) // 위에서 저장한 member 객체 주입
                    .provider(provider)
                    .providerUserId(providerUserId)
                    .build();
            socialAccountRepository.save(socialAccount);

            System.out.println("🎉 신규 소셜 회원가입 완료: " + memberId);
        } else {
            // [기존 회원 로그인]
            System.out.println("🚀 기존 소셜 회원 로그인: " + socialOpt.get().getMember().getMemberid());
        }

        return oAuth2User;
    }
}