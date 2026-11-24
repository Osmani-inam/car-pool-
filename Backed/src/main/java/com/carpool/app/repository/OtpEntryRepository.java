package com.carpool.app.repository;

import com.carpool.app.entity.OtpEntry;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpEntryRepository extends JpaRepository<OtpEntry, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OtpEntry> findFirstByPhoneAndUsedFalseOrderByCreatedAtDesc(String phone);

    @Modifying
    @Transactional
    @Query("update OtpEntry entry set entry.used = true where entry.phone = :phone and entry.used = false")
    int markUnusedByPhoneAsUsed(@Param("phone") String phone);

    @Modifying
    @Transactional
    @Query("delete from OtpEntry entry where entry.expiresAt < :cutoff")
    int deleteExpired(@Param("cutoff") LocalDateTime cutoff);
}
