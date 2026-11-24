package com.carpool.app.controller;

import com.carpool.app.dto.BookingDtos.BookingRequestResponse;
import com.carpool.app.security.CurrentUser;
import com.carpool.app.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/api/rides/{rideId}/seats/{seatId}/request")
    public BookingRequestResponse requestSeat(@PathVariable Long rideId, @PathVariable Long seatId) {
        return bookingService.requestSeat(CurrentUser.get(), rideId, seatId);
    }

    @GetMapping("/api/requests/incoming")
    public List<BookingRequestResponse> incomingRequests() {
        return bookingService.incomingRequests(CurrentUser.get());
    }

    @GetMapping("/api/requests/my")
    public List<BookingRequestResponse> myRequests() {
        return bookingService.myRequests(CurrentUser.get());
    }

    @PatchMapping("/api/requests/{id}/accept")
    public BookingRequestResponse accept(@PathVariable Long id) {
        return bookingService.accept(CurrentUser.get(), id);
    }

    @PatchMapping("/api/requests/{id}/reject")
    public BookingRequestResponse reject(@PathVariable Long id) {
        return bookingService.reject(CurrentUser.get(), id);
    }

    @DeleteMapping("/api/requests/{id}")
    public void cancel(@PathVariable Long id) {
        bookingService.cancelOwnRequest(CurrentUser.get(), id);
    }
}
