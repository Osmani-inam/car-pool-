package com.carpool.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class AuthDtos {

    @Data
    public static class SignupRequest {
        @NotBlank private String name;
        @NotBlank private String phone;
    }

    @Data
    public static class LoginRequest {
        @NotBlank private String phone;
        @NotBlank private String otp;

        public LoginRequest() {}

        public LoginRequest(String phone, String otp) {
            this.phone = phone;
            this.otp = otp;
        }
    }

    @Data
    public static class OtpRequest {
        @NotBlank private String phone;
    }

    @Data
    public static class OtpResponse {
        private final String message;
        private final long expiresInSeconds;
        private final long resendInSeconds;

        public OtpResponse(String message, long expiresInSeconds, long resendInSeconds) {
            this.message = message;
            this.expiresInSeconds = expiresInSeconds;
            this.resendInSeconds = resendInSeconds;
        }
    }

    @Data
    public static class AuthResponse {
        private String token;
        private Long userId;
        private String name;

        public AuthResponse(String token, Long userId, String name) {
            this.token = token;
            this.userId = userId;
            this.name = name;
        }
    }
}
