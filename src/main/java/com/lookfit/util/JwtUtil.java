//package com.lookfit.util;
//
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.security.Keys;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import javax.crypto.SecretKey;
//import java.nio.charset.StandardCharsets;
//import java.util.Date;
//
//@Component
//public class JwtUtil {
//
//    private final SecretKey key;
//    private final long expirationTime;
//
//    public JwtUtil(@Value("${jwt.secret}") String secret,
//                   @Value("${jwt.expiration}") long expirationTime) {
//        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
//        this.expirationTime = expirationTime;
//    }
//
//    public String createToken(String memberid, String role) {
//        Date now = new Date();
//        Date expiryDate = new Date(now.getTime() + expirationTime);
//
//        return Jwts.builder()
//                .subject(memberid)
//                .claim("role", role)
//                .issuedAt(now)
//                .expiration(expiryDate)
//                .signWith(key)
//                .compact();
//    }
//}