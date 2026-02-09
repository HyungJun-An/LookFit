package com.lookfit.cart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lookfit.auth.dto.AuthDto;
import com.lookfit.cart.dto.CartDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CartController 통합 테스트 (JWT 인증)
 *
 * 테스트 계정: test@test.com / test1234
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_EMAIL = "test@test.com";
    private static final String TEST_PASSWORD = "test1234";

    private String jwtToken;

    @BeforeEach
    void setUp() throws Exception {
        // 로그인하여 JWT 토큰 발급
        AuthDto.LoginRequest loginRequest = new AuthDto.LoginRequest();
        loginRequest.setEmail(TEST_EMAIL);
        loginRequest.setPassword(TEST_PASSWORD);

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        AuthDto.AuthResponse authResponse = objectMapper.readValue(responseBody, AuthDto.AuthResponse.class);
        jwtToken = authResponse.getToken();
    }

    @Test
    @DisplayName("장바구니 조회 성공 (JWT 인증)")
    void getCartSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/cart")
                        .header("Authorization", "Bearer " + jwtToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    @DisplayName("장바구니 조회 실패 - JWT 토큰 없음")
    void getCartFailWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/cart"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("장바구니 조회 실패 - 잘못된 JWT 토큰")
    void getCartFailWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/cart")
                        .header("Authorization", "Bearer invalid_token"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("장바구니 추가 성공 (JWT 인증)")
    void addToCartSuccess() throws Exception {
        // given
        CartDto.AddRequest request = new CartDto.AddRequest();
        request.setProductId("P001"); // 실제 존재하는 상품 ID로 변경 필요
        request.setAmount(2);  // amount 필드 사용

        // when & then
        mockMvc.perform(post("/api/v1/cart")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("장바구니 추가 실패 - JWT 토큰 없음")
    void addToCartFailWithoutToken() throws Exception {
        // given
        CartDto.AddRequest request = new CartDto.AddRequest();
        request.setProductId("P001");
        request.setAmount(2);  // amount 필드 사용

        // when & then
        mockMvc.perform(post("/api/v1/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("장바구니 수량 변경 성공")
    void updateCartAmountSuccess() throws Exception {
        // given
        String productId = "P001"; // 실제 존재하는 상품 ID
        CartDto.UpdateRequest request = new CartDto.UpdateRequest();  // UpdateRequest 사용
        request.setAmount(5);  // amount 필드 사용

        // when & then
        mockMvc.perform(patch("/api/v1/cart/{productId}", productId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("장바구니 삭제 성공")
    void removeFromCartSuccess() throws Exception {
        // given
        String productId = "P001"; // 실제 존재하는 상품 ID

        // when & then
        mockMvc.perform(delete("/api/v1/cart/{productId}", productId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
