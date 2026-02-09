package com.lookfit.auth.controller;

import com.lookfit.auth.dto.AuthDto;
import com.lookfit.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입
     * POST /api/v1/auth/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthDto.AuthResponse> signup(@RequestBody AuthDto.SignupRequest request) {
        log.info("Signup request received: email={}", request.getEmail());
        AuthDto.AuthResponse response = authService.signup(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 로그인
     * POST /api/v1/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthDto.AuthResponse> login(@RequestBody AuthDto.LoginRequest request) {
        log.info("Login request received: email={}", request.getEmail());
        AuthDto.AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
