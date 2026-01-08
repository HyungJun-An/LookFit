package com.lookfit.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // 실제로는 application.yml에서 관리하고 보안을 위해 길게 작성해야 합니다.
    @Value("${jwt_secret_key}")
    private  String secretKey;
    private final long tokenValidTime = 30 * 60 * 1000L; // 30분

    private Key key;

    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // 토큰 생성
    public String createToken(String memberId, String role) {
        Date now = new Date();
        System.out.println("토큰 생성중");
        return Jwts.builder()
                .setSubject(memberId)               // 대상자 (memberid)
                .claim("role", role)                // 권한 정보 (커스텀 클레임)
                .setIssuedAt(now)                   // 발행 시간
                .setExpiration(new Date(now.getTime() + tokenValidTime)) // 만료 시간
                .signWith(key, SignatureAlgorithm.HS256) // 서명
                .compact();                         // 생성
    }

    // 토큰에서 memberId 추출
    public String getMemberId(String token) {
        return Jwts.parser().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(key).build().parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}