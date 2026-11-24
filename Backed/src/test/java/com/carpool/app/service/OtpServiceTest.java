package com.carpool.app.service;

import com.carpool.app.entity.OtpEntry;
import com.carpool.app.repository.OtpEntryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private OtpEntryRepository otpEntryRepository;

    @InjectMocks
    private OtpService otpService;

    @Test
    void issueOtpInvalidatesOlderUnusedEntriesAndPersistsSixDigitOtp() {
        when(otpEntryRepository.save(any(OtpEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OtpEntry issued = otpService.issueOtp("9990001111");

        assertEquals("9990001111", issued.getPhone());
        assertTrue(issued.getCode().matches("\\d{6}"));
        assertFalse(issued.isUsed());
        assertTrue(issued.getExpiresAt().isAfter(LocalDateTime.now().plusMinutes(4)));
        verify(otpEntryRepository).markUnusedByPhoneAsUsed("9990001111");
        verify(otpEntryRepository).save(issued);
    }

    @Test
    void validateOtpMarksMatchingUnexpiredEntryUsed() {
        OtpEntry entry = OtpEntry.builder()
                .phone("9990001111")
                .code("123456")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .createdAt(LocalDateTime.now())
                .build();
        when(otpEntryRepository.findFirstByPhoneAndUsedFalseOrderByCreatedAtDesc("9990001111"))
                .thenReturn(Optional.of(entry));
        when(otpEntryRepository.save(entry)).thenReturn(entry);

        OtpEntry validated = otpService.validateOtp("9990001111", "123456");

        assertSame(entry, validated);
        assertTrue(entry.isUsed());
        verify(otpEntryRepository).save(entry);
    }

    @Test
    void validateOtpRejectsWrongCodeAndExpiredEntryWithoutMarkingItUsed() {
        OtpEntry entry = OtpEntry.builder()
                .phone("9990001111")
                .code("123456")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .createdAt(LocalDateTime.now())
                .build();
        when(otpEntryRepository.findFirstByPhoneAndUsedFalseOrderByCreatedAtDesc("9990001111"))
                .thenReturn(Optional.of(entry));

        IllegalArgumentException invalidCode = assertThrows(
                IllegalArgumentException.class, () -> otpService.validateOtp("9990001111", "000000"));
        assertEquals("Invalid OTP", invalidCode.getMessage());

        entry.setExpiresAt(LocalDateTime.now().minusSeconds(1));
        assertThrows(IllegalArgumentException.class, () -> otpService.validateOtp("9990001111", "123456"));
        assertFalse(entry.isUsed());
        verify(otpEntryRepository, never()).save(any(OtpEntry.class));
    }
}
