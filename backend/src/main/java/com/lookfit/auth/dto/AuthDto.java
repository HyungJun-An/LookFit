package com.lookfit.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

public class AuthDto {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignupRequest {
        @JsonProperty("email")
        private String email;

        @JsonProperty("password")
        private String password;

        @JsonProperty("memberName")
        private String memberName;

        @JsonProperty("phone")
        private String phone;

        @JsonProperty("gender")
        private String gender; // M, F

        @JsonProperty("age")
        private Integer age;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @JsonProperty("email")
        private String email;

        @JsonProperty("password")
        private String password;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthResponse {
        @JsonProperty("token")
        private String token;

        @JsonProperty("memberId")
        private String memberId;

        @JsonProperty("memberName")
        private String memberName;

        @JsonProperty("email")
        private String email;

        @JsonProperty("grade")
        private String grade;
    }
}
