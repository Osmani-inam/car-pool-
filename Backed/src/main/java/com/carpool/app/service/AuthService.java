package com.carpool.app.service;

import com.carpool.app.dto.AuthDtos.*;
import com.carpool.app.entity.OtpEntry;
import com.carpool.app.entity.User;
import com.carpool.app.repository.UserRepository;
import com.carpool.app.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SmsProvider smsProvider;
    private final OtpService otpService;

    public AuthResponse signup(SignupRequest req) {
        if (userRepository.existsByPhone(req.getPhone())) {
            throw new IllegalArgumentException("Phone number already registered");
        }
        User user = User.builder()
                .name(req.getName())
                .phone(req.getPhone())
                .passwordHash(passwordEncoder.encode("otp-default-" + UUID.randomUUID()))
                .build();
        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getPhone());
        return new AuthResponse(token, user.getId(), user.getName());
    }

    public OtpResponse requestOtp(String phone) {
        String normalizedPhone = normalizePhone(phone);
        userRepository.findByPhone(normalizedPhone).orElseGet(() -> createOtpUser(normalizedPhone));

        OtpEntry otpEntry = otpService.issueOtp(normalizedPhone);
        try {
            smsProvider.sendOtp(normalizedPhone, otpEntry.getCode());
        } catch (RuntimeException e) {
            otpService.clearOtp(normalizedPhone);
            throw e;
        }

        return new OtpResponse(
                "OTP requested for " + normalizedPhone + ". Check your SMS or the backend console when using MOCK.",
                otpEntry.getExpiresInSeconds(),
                otpService.getResendAfterSeconds(normalizedPhone)
        );
    }

    public OtpResponse resendOtp(String phone) {
        return requestOtp(phone);
    }

    public AuthResponse login(LoginRequest req) {
        String normalizedPhone = normalizePhone(req.getPhone());
        User user = userRepository.findByPhone(normalizedPhone)
                .orElseThrow(() -> new IllegalArgumentException("Invalid phone number"));

        otpService.validateOtp(normalizedPhone, req.getOtp());

        String token = jwtUtil.generateToken(user.getId(), user.getPhone());
        return new AuthResponse(token, user.getId(), user.getName());
    }

    private String normalizePhone(String phone) {
        if (phone == null) {
            throw new IllegalArgumentException("Phone number is required");
        }
        return phone.trim();
    }

    private User createOtpUser(String phone) {
        User user = User.builder()
                .name("Carpool user")
                .phone(phone)
                .passwordHash(passwordEncoder.encode("otp-default-" + UUID.randomUUID()))
                .build();
        return userRepository.save(user);
    }
}
