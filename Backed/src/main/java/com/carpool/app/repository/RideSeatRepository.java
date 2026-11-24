package com.carpool.app.repository;

import com.carpool.app.entity.RideSeat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RideSeatRepository extends JpaRepository<RideSeat, Long> {
    List<RideSeat> findByRideId(Long rideId);
    List<RideSeat> findByStatusAndHeldUntilBefore(RideSeat.Status status, LocalDateTime cutoff);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select seat from RideSeat seat join fetch seat.ride where seat.id = :seatId")
    Optional<RideSeat> findByIdForUpdate(@Param("seatId") Long seatId);
}
