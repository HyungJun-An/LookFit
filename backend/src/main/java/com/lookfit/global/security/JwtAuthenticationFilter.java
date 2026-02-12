package com.lookfit.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);
        String requestUri = request.getRequestURI();

        if (logger.isDebugEnabled()) {
            logger.debug("🔍 JWT Filter - URI: " + requestUri + ", Token exists: " + (token != null));
        }

        if (StringUtils.hasText(token)) {
            // 테스트용 고정 토큰 체크
            if ("test_token_fitting".equals(token)) {
                logger.warn("⚠️ 테스트용 고정 토큰 사용 - memberId: google_11510");
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                "google_11510",
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            // 실제 JWT 토큰 검증
            else {
                boolean isValid = jwtTokenProvider.validateToken(token);

                if (isValid) {
                    String memberId = jwtTokenProvider.getMemberId(token);
                    String role = jwtTokenProvider.getRole(token);

                    logger.info("✅ JWT Auth success - memberId: " + memberId + ", role: " + role);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    memberId,
                                    null,
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                            );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    logger.warn("❌ JWT Token validation failed for URI: " + requestUri);
                }
            }
        } else {
            if (requestUri.startsWith("/api/v1/products/") && requestUri.contains("/reviews")
                && request.getMethod().equals("POST")) {
                logger.warn("⚠️ No JWT token for review creation: " + requestUri);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
