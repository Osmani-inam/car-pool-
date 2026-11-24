package com.carpool.app.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.carpool.app.dto.RideDtos.CreateRideRequest;
import com.carpool.app.dto.RideDtos.RideResponse;
import com.carpool.app.dto.RideDtos.UpcomingRidePublicResponse;
import com.carpool.app.entity.Ride;
import com.carpool.app.security.CurrentUser;
import com.carpool.app.service.RideService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
public class RideController {

    private final RideService rideService;

    @PostMapping
    public RideResponse createRide(@Valid @RequestBody CreateRideRequest req) {
        return rideService.createRide(CurrentUser.get(), req);
    }

    @GetMapping("/search")
    public List<RideResponse> search(@RequestParam String origin,
                                      @RequestParam String destination,
                                      @RequestParam @DateTimeFormat(pattern = "dd/MM/yy") LocalDate date) {
        return rideService.search(origin, destination, date);
    }

    @GetMapping("/upcoming")
    public List<UpcomingRidePublicResponse> upcomingPublic() {
        return rideService.upcomingPublic();
    }

    @GetMapping("/ended")
    public List<UpcomingRidePublicResponse> endedPublic() {
        return rideService.endedPublic();
    }

    @GetMapping("/my")
    public List<RideResponse> myRides() {
        return rideService.myRides(CurrentUser.get());
    }

    @GetMapping("/{id:\\d+}")
    public RideResponse getById(@PathVariable Long id) {
        return rideService.getById(id);
    }

    @PatchMapping("/{id:\\d+}/status")
    public RideResponse updateStatus(@PathVariable Long id, @RequestParam Ride.Status status) {
        return rideService.updateStatus(CurrentUser.get(), id, status);
    }
}
