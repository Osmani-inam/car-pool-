package com.carpool.app.repository;

import com.carpool.app.entity.Ride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RideRepository extends JpaRepository<Ride, Long> {

    List<Ride> findByOriginIgnoreCaseAndDestinationIgnoreCaseAndRideDateAndStatus(
            String origin, String destination, LocalDate rideDate, Ride.Status status);

    List<Ride> findByStatus(Ride.Status status);

    List<Ride> findByOwnerId(Long ownerId);
}
