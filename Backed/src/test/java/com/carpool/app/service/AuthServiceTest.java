package com.carpool.app.service;

import com.carpool.app.dto.AuthDtos.*;
import com.carpool.app.entity.OtpEntry;
import com.carpool.app.entity.User;
import com.carpool.app.repository.UserRepository;
import com.carpool.app.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private SmsProvider smsProvider;

    @Mock
    private OtpService otpService;

    @InjectMocks
    private AuthService authService;

    @Test
    void signupAndLoginShouldWorkWithPhoneAndOtpOnly() {
        SignupRequest signupReq = new SignupRequest();
        signupReq.setName("Aisha");
        signupReq.setPhone("9990001111");

        when(userRepository.existsByPhone("9990001111")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(10L);
            return user;
        });
        when(jwtUtil.generateToken(10L, "9990001111")).thenReturn("jwt-token");

        AuthResponse signupResponse = authService.signup(signupReq);
        assertNotNull(signupResponse);
        assertEquals("jwt-token", signupResponse.getToken());

        OtpEntry otpEntry = OtpEntry.builder()
                .phone("9990001111")
                .code("123456")
                .expiresAt(java.time.LocalDateTime.now().plusMinutes(5))
                .createdAt(java.time.LocalDateTime.now())
                .build();
        when(otpService.issueOtp("9990001111")).thenReturn(otpEntry);

        OtpResponse otpResponse = authService.requestOtp("9990001111");
        assertNotNull(otpResponse);
        assertTrue(otpResponse.getMessage().contains("OTP requested"));
        assertFalse(otpResponse.getMessage().contains("123456"));
        verify(smsProvider).sendOtp("9990001111", "123456");

        when(userRepository.findByPhone("9990001111")).thenReturn(java.util.Optional.of(User.builder()
                .id(10L)
                .name("Aisha")
                .phone("9990001111")
                .passwordHash("hashed-password")
                .build()));

        when(otpService.validateOtp("9990001111", "123456")).thenReturn(otpEntry);

        AuthResponse loginResponse = authService.login(new LoginRequest("9990001111", "123456"));
        assertNotNull(loginResponse);
        assertEquals("jwt-token", loginResponse.getToken());
    }
}
