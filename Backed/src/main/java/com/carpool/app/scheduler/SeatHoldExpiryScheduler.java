package com.carpool.app.scheduler;

import com.carpool.app.entity.BookingRequest;
import com.carpool.app.entity.RideSeat;
import com.carpool.app.repository.BookingRequestRepository;
import com.carpool.app.repository.RideSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Runs every minute. Any seat still HELD past its held_until timestamp
 * (owner never responded to the request) is released back to AVAILABLE,
 * and the corresponding pending request is marked CANCELLED.
 */
@Component
@RequiredArgsConstructor
public class SeatHoldExpiryScheduler {

    private final RideSeatRepository rideSeatRepository;
    private final BookingRequestRepository bookingRequestRepository;

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void releaseExpiredHolds() {
        List<RideSeat> expired = rideSeatRepository
                .findByStatusAndHeldUntilBefore(RideSeat.Status.HELD, LocalDateTime.now());

        for (RideSeat seat : expired) {
            seat.setStatus(RideSeat.Status.AVAILABLE);
            seat.setHeldUntil(null);
            seat.setBookedBy(null);
            rideSeatRepository.save(seat);

            bookingRequestRepository.findBySeatIdAndStatus(seat.getId(), BookingRequest.Status.PENDING)
                    .forEach(request -> {
                        request.setStatus(BookingRequest.Status.CANCELLED);
                        bookingRequestRepository.save(request);
                    });
        }
    }
}
