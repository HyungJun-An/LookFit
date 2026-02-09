package com.lookfit.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lookfit.auth.dto.AuthDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 통합 테스트
 * 
 * 테스트 계정: test@test.com / test1234
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_EMAIL = "test@test.com";
    private static final String TEST_PASSWORD = "test1234";

    @Test
    @DisplayName("로그인 성공 - JWT 토큰 발급")
    void loginSuccess() throws Exception {
        // given
        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);

        // when & then
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.memberId").exists())
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andReturn();

        // JWT 토큰 검증
        String responseBody = result.getResponse().getContentAsString();
        AuthDto.AuthResponse response = objectMapper.readValue(responseBody, AuthDto.AuthResponse.class);
        
        assertThat(response.getToken()).isNotNull();
        assertThat(response.getToken()).startsWith("eyJ"); // JWT 토큰 형식
        assertThat(response.getEmail()).isEqualTo(TEST_EMAIL);
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void loginFailWithWrongPassword() throws Exception {
        // given
        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword("wrongpassword");

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    void loginFailWithNonExistentEmail() throws Exception {
        // given
        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setEmail("nonexistent@test.com");
        request.setPassword(TEST_PASSWORD);

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("로그인 실패 - 필수 파라미터 누락 (이메일)")
    void loginFailWithMissingEmail() throws Exception {
        // given
        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setPassword(TEST_PASSWORD);

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }
}
