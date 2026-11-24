package com.carpool.app.repository;

import com.carpool.app.entity.BookingRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRequestRepository extends JpaRepository<BookingRequest, Long> {
    List<BookingRequest> findByPassengerId(Long passengerId);
    List<BookingRequest> findByRideOwnerId(Long ownerId);
    List<BookingRequest> findByRide_Owner_IdAndStatus(Long ownerId, BookingRequest.Status status);
    List<BookingRequest> findBySeatIdAndStatus(Long seatId, BookingRequest.Status status);
}
