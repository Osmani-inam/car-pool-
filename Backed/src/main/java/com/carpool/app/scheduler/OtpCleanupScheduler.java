package com.carpool.app.scheduler;

import com.carpool.app.repository.OtpEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OtpCleanupScheduler {

    private final OtpEntryRepository otpEntryRepository;

    @Scheduled(fixedRate = 600_000)
    public void deleteExpiredOtps() {
        otpEntryRepository.deleteExpired(LocalDateTime.now());
    }
}
