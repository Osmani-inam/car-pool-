package com.carpool.app.service;

import com.carpool.app.entity.OtpEntry;
import com.carpool.app.repository.OtpEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(30);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final OtpEntryRepository otpEntryRepository;
    private final ConcurrentHashMap<String, LocalDateTime> resendAfterByPhone = new ConcurrentHashMap<>();

    @Transactional
    public OtpEntry issueOtp(String phone) {
        String normalized = normalizePhone(phone);
        LocalDateTime resendAfter = resendAfterByPhone.get(normalized);
        if (resendAfter != null && LocalDateTime.now().isBefore(resendAfter)) {
            long remainingSeconds = Math.max(0, Duration.between(LocalDateTime.now(), resendAfter).getSeconds());
            throw new IllegalArgumentException("Please wait " + remainingSeconds + " seconds before requesting a new OTP.");
        }

        otpEntryRepository.markUnusedByPhoneAsUsed(normalized);

        LocalDateTime now = LocalDateTime.now();
        String code = generateOtp();
        OtpEntry entry = OtpEntry.builder()
                .phone(normalized)
                .code(code)
                .expiresAt(now.plus(OTP_TTL))
                .createdAt(now)
                .build();
        OtpEntry savedEntry = otpEntryRepository.save(entry);
        resendAfterByPhone.put(normalized, now.plus(RESEND_COOLDOWN));
        return savedEntry;
    }

    @Transactional
    public OtpEntry validateOtp(String phone, String suppliedOtp) {
        String normalized = normalizePhone(phone);
        OtpEntry entry = otpEntryRepository.findFirstByPhoneAndUsedFalseOrderByCreatedAtDesc(normalized)
                .orElseThrow(() -> new IllegalArgumentException("OTP expired. Please request a new OTP."));
        if (!entry.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP expired. Please request a new OTP.");
        }
        if (!entry.getCode().equals(String.valueOf(suppliedOtp).trim())) {
            throw new IllegalArgumentException("Invalid OTP");
        }
        entry.setUsed(true);
        otpEntryRepository.save(entry);
        resendAfterByPhone.remove(normalized);
        return entry;
    }

    @Transactional
    public void clearOtp(String phone) {
        String normalized = normalizePhone(phone);
        otpEntryRepository.markUnusedByPhoneAsUsed(normalized);
        resendAfterByPhone.remove(normalized);
    }

    public long getResendAfterSeconds(String phone) {
        LocalDateTime resendAfter = resendAfterByPhone.get(normalizePhone(phone));
        if (resendAfter == null) {
            return 0;
        }
        return Math.max(0, Duration.between(LocalDateTime.now(), resendAfter).getSeconds());
    }

    private String normalizePhone(String phone) {
        if (phone == null) {
            throw new IllegalArgumentException("Phone number is required");
        }
        return phone.trim();
    }

    private String generateOtp() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }
}
