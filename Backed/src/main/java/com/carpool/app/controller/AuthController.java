package com.carpool.app.controller;

import com.carpool.app.dto.AuthDtos.*;
import com.carpool.app.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public AuthResponse signup(@Valid @RequestBody SignupRequest req) {
        return authService.signup(req);
    }

    @PostMapping("/send-otp")
    public OtpResponse sendOtp(@Valid @RequestBody OtpRequest req) {
        return authService.requestOtp(req.getPhone());
    }

    @PostMapping("/resend-otp")
    public OtpResponse resendOtp(@Valid @RequestBody OtpRequest req) {
        return authService.resendOtp(req.getPhone());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }
}
