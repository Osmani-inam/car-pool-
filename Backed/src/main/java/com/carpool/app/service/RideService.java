package com.carpool.app.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.carpool.app.dto.RideDtos.CreateRideRequest;
import com.carpool.app.dto.RideDtos.RideResponse;
import com.carpool.app.dto.RideDtos.SeatDto;
import com.carpool.app.dto.RideDtos.UpcomingRidePublicResponse;
import com.carpool.app.entity.Car;
import com.carpool.app.entity.Ride;
import com.carpool.app.entity.RideSeat;
import com.carpool.app.entity.User;
import com.carpool.app.repository.CarRepository;
import com.carpool.app.repository.RideRepository;
import com.carpool.app.repository.RideSeatRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RideService {

    private final RideRepository rideRepository;
    private final CarRepository carRepository;
    private final RideSeatRepository rideSeatRepository;

    public RideResponse createRide(User owner, CreateRideRequest req) {
        Car car = carRepository.findById(req.getCarId())
                .orElseThrow(() -> new IllegalArgumentException("Car not found"));

        if (!car.getOwner().getId().equals(owner.getId())) {
            throw new IllegalArgumentException("You do not own this car");
        }

        Ride ride = Ride.builder()
                .owner(owner)
                .car(car)
                .origin(req.getOrigin())
                .destination(req.getDestination())
                .pickupPoint(req.getPickupPoint())
                .rideDate(req.getRideDate())
                .rideTime(req.getRideTime())
                .endTime(req.getEndTime())
                .pricePerSeat(req.getPricePerSeat())
                .status(Ride.Status.UPCOMING)
                .build();
        if (!req.getEndTime().isAfter(req.getRideTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        ride = rideRepository.save(ride);

        // auto-generate ride_seats from the car's seat layout
        for (String label : car.getSeatLayout()) {
            RideSeat seat = RideSeat.builder()
                    .ride(ride)
                    .seatLabel(label)
                    .status(RideSeat.Status.AVAILABLE)
                    .build();
            rideSeatRepository.save(seat);
        }

        return toResponse(ride);
    }

    public List<RideResponse> search(String origin, String destination, LocalDate date) {
        return rideRepository
                .findByOriginIgnoreCaseAndDestinationIgnoreCaseAndRideDateAndStatus(
                        origin, destination, date, Ride.Status.UPCOMING)
            .stream().filter(ride -> !hasEnded(ride))
            .sorted(Comparator.comparing(Ride::getRideDate).thenComparing(Ride::getRideTime))
            .map(this::toResponse).collect(Collectors.toList());
    }

    public RideResponse getById(Long id) {
        Ride ride = rideRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ride not found"));
        return toResponse(ride);
    }

    public List<UpcomingRidePublicResponse> upcomingPublic() {
        return rideRepository.findByStatus(Ride.Status.UPCOMING)
                .stream().filter(ride -> !hasEnded(ride))
                .sorted(Comparator.comparing(Ride::getRideDate).thenComparing(Ride::getRideTime))
                .map(this::toPublicResponse).collect(Collectors.toList());
    }

    public List<UpcomingRidePublicResponse> endedPublic() {
        return Stream.concat(
                rideRepository.findByStatus(Ride.Status.UPCOMING).stream()
                    .filter(this::hasEnded),
                rideRepository.findByStatus(Ride.Status.COMPLETED).stream())
                .sorted(Comparator.comparing(Ride::getRideDate).thenComparing(Ride::getRideTime))
                .map(this::toPublicResponse)
                .collect(Collectors.toList());
    }

    private UpcomingRidePublicResponse toPublicResponse(Ride ride) {
        UpcomingRidePublicResponse r = new UpcomingRidePublicResponse();
        r.setId(ride.getId());
        r.setOrigin(ride.getOrigin());
        r.setDestination(ride.getDestination());
        r.setRideDate(ride.getRideDate());
        r.setRideTime(ride.getRideTime());
        r.setEndTime(ride.getEndTime() != null ? ride.getEndTime() : ride.getRideTime());
        r.setOwnerName(ride.getOwner().getName());
        r.setCarModel(ride.getCar().getModel());
        r.setPricePerSeat(ride.getPricePerSeat());
        long seatsLeft = rideSeatRepository.findByRideId(ride.getId()).stream()
                .filter(s -> s.getStatus() == RideSeat.Status.AVAILABLE)
                .count();
        r.setSeatsLeft(seatsLeft);
        return r;
    }

    private boolean hasEnded(Ride ride) {
        LocalTime effectiveEndTime = ride.getEndTime() != null
                ? ride.getEndTime()
                : ride.getRideTime();
        return !LocalDateTime.of(ride.getRideDate(), effectiveEndTime).isAfter(LocalDateTime.now());
    }

    public List<RideResponse> myRides(User user) {
        return rideRepository.findByOwnerId(user.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public RideResponse updateStatus(User owner, Long rideId, Ride.Status newStatus) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Ride not found"));
        if (!ride.getOwner().getId().equals(owner.getId())) {
            throw new IllegalArgumentException("Only the owner can update this ride");
        }
        ride.setStatus(newStatus);
        ride = rideRepository.save(ride);
        return toResponse(ride);
    }

    private RideResponse toResponse(Ride ride) {
        RideResponse r = new RideResponse();
        r.setId(ride.getId());
        r.setOwnerName(ride.getOwner().getName());
        r.setCarModel(ride.getCar().getModel());
        r.setOrigin(ride.getOrigin());
        r.setDestination(ride.getDestination());
        r.setPickupPoint(ride.getPickupPoint());
        r.setRideDate(ride.getRideDate());
        r.setRideTime(ride.getRideTime());
        r.setEndTime(ride.getEndTime());
        r.setPricePerSeat(ride.getPricePerSeat());
        r.setStatus(ride.getStatus().name());

        List<SeatDto> seats = rideSeatRepository.findByRideId(ride.getId()).stream()
                .map(s -> {
                    SeatDto d = new SeatDto();
                    d.setId(s.getId());
                    d.setSeatLabel(s.getSeatLabel());
                    d.setStatus(s.getStatus().name());
                    return d;
                }).collect(Collectors.toList());
        r.setSeats(seats);
        return r;
    }
}
