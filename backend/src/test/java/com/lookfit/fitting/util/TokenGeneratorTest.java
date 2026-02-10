package com.lookfit.fitting.util;

import com.lookfit.global.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * JWT 토큰 생성 테스트 유틸리티
 */
@SpringBootTest
public class TokenGeneratorTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void generateTestToken() {
        String memberId = "google_11510";
        String role = "USER";

        String token = jwtTokenProvider.createToken(memberId, role);

        System.out.println("\n========================================");
        System.out.println("테스트용 JWT 토큰:");
        System.out.println(token);
        System.out.println("========================================\n");
        System.out.println("사용 방법:");
        System.out.println("curl -X POST http://localhost:8080/api/v1/fitting/upload \\");
        System.out.println("  -H \"Authorization: Bearer " + token + "\" \\");
        System.out.println("  -F \"productId=P001\" \\");
        System.out.println("  -F \"category=upper_body\" \\");
        System.out.println("  -F \"image=@/tmp/fitting_test/test_user.jpg\"");
        System.out.println("========================================\n");
    }
}
