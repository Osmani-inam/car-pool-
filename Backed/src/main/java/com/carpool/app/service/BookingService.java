package com.carpool.app.service;

import com.carpool.app.dto.BookingDtos.BookingRequestResponse;
import com.carpool.app.entity.*;
import com.carpool.app.repository.BookingRequestRepository;
import com.carpool.app.repository.RideSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final RideSeatRepository rideSeatRepository;
    private final BookingRequestRepository bookingRequestRepository;

    @Value("${app.seat.hold-minutes:10}")
    private int holdMinutes;

    @Transactional
    public BookingRequestResponse requestSeat(User passenger, Long rideId, Long seatId) {
        RideSeat seat = rideSeatRepository.findByIdForUpdate(seatId)
                .orElseThrow(() -> new IllegalArgumentException("Seat not found"));

        if (!seat.getRide().getId().equals(rideId)) {
            throw new IllegalArgumentException("Seat does not belong to this ride");
        }
        if (seat.getStatus() != RideSeat.Status.AVAILABLE) {
            throw new IllegalStateException("Seat is not available");
        }

        seat.setStatus(RideSeat.Status.HELD);
        seat.setHeldUntil(LocalDateTime.now().plusMinutes(holdMinutes));
        seat.setBookedBy(passenger);
        rideSeatRepository.save(seat);

        BookingRequest request = BookingRequest.builder()
                .ride(seat.getRide())
                .seat(seat)
                .passenger(passenger)
                .status(BookingRequest.Status.PENDING)
                .build();
        request = bookingRequestRepository.save(request);

        return toResponse(request);
    }

    public List<BookingRequestResponse> incomingRequests(User owner) {
        return bookingRequestRepository.findByRide_Owner_IdAndStatus(owner.getId(), BookingRequest.Status.PENDING).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<BookingRequestResponse> myRequests(User passenger) {
        return bookingRequestRepository.findByPassengerId(passenger.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookingRequestResponse accept(User owner, Long requestId) {
        BookingRequest request = getOwnedRequest(owner, requestId);
        requirePending(request);
        request.setStatus(BookingRequest.Status.ACCEPTED);
        request.setRespondedAt(LocalDateTime.now());
        bookingRequestRepository.save(request);

        RideSeat seat = request.getSeat();
        seat.setStatus(RideSeat.Status.BOOKED);
        seat.setHeldUntil(null);
        rideSeatRepository.save(seat);

        return toResponse(request);
    }

    @Transactional
    public BookingRequestResponse reject(User owner, Long requestId) {
        BookingRequest request = getOwnedRequest(owner, requestId);
        requirePending(request);
        request.setStatus(BookingRequest.Status.REJECTED);
        request.setRespondedAt(LocalDateTime.now());
        bookingRequestRepository.save(request);

        RideSeat seat = request.getSeat();
        seat.setStatus(RideSeat.Status.AVAILABLE);
        seat.setHeldUntil(null);
        seat.setBookedBy(null);
        rideSeatRepository.save(seat);

        return toResponse(request);
    }

    @Transactional
    public void cancelOwnRequest(User passenger, Long requestId) {
        BookingRequest request = bookingRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        if (!request.getPassenger().getId().equals(passenger.getId())) {
            throw new IllegalArgumentException("Not your request");
        }
        if (request.getStatus() != BookingRequest.Status.PENDING
                && request.getStatus() != BookingRequest.Status.ACCEPTED) {
            throw new IllegalStateException("This request can no longer be cancelled");
        }
        request.setStatus(BookingRequest.Status.CANCELLED);
        bookingRequestRepository.save(request);

        RideSeat seat = request.getSeat();
        seat.setStatus(RideSeat.Status.AVAILABLE);
        seat.setHeldUntil(null);
        seat.setBookedBy(null);
        rideSeatRepository.save(seat);
    }

    private BookingRequest getOwnedRequest(User owner, Long requestId) {
        BookingRequest request = bookingRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        if (!request.getRide().getOwner().getId().equals(owner.getId())) {
            throw new IllegalArgumentException("Only the ride owner can respond to this request");
        }
        return request;
    }

    private void requirePending(BookingRequest request) {
        if (request.getStatus() != BookingRequest.Status.PENDING) {
            throw new IllegalStateException("This request is no longer pending");
        }
    }

    private BookingRequestResponse toResponse(BookingRequest r) {
        BookingRequestResponse d = new BookingRequestResponse();
        d.setId(r.getId());
        d.setRideId(r.getRide().getId());
        d.setSeatLabel(r.getSeat().getSeatLabel());
        d.setPassengerName(r.getPassenger().getName());
        if (r.getStatus() == BookingRequest.Status.ACCEPTED) {
            d.setOwnerName(r.getRide().getOwner().getName());
            d.setOwnerPhone(r.getRide().getOwner().getPhone());
        }
        d.setStatus(r.getStatus().name());
        d.setRequestedAt(r.getRequestedAt());
        return d;
    }
}
