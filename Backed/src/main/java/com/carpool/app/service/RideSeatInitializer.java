package com.carpool.app.service;

import com.carpool.app.entity.Car;
import com.carpool.app.entity.Ride;
import com.carpool.app.entity.RideSeat;
import com.carpool.app.repository.RideRepository;
import com.carpool.app.repository.RideSeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RideSeatInitializer implements ApplicationRunner {

    private final RideRepository rideRepository;
    private final RideSeatRepository rideSeatRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int restoredRides = 0;
        int restoredSeats = 0;

        for (Ride ride : rideRepository.findAll()) {
            if (!rideSeatRepository.findByRideId(ride.getId()).isEmpty()) {
                continue;
            }

            Car car = ride.getCar();
            List<String> seatLayout = car.getSeatLayout();
            if (seatLayout == null || seatLayout.isEmpty()) {
                log.warn("Cannot restore seats for ride {}: car {} has no seat layout",
                        ride.getId(), car.getId());
                continue;
            }

            List<RideSeat> seats = new ArrayList<>();
            for (String label : seatLayout) {
                seats.add(RideSeat.builder()
                        .ride(ride)
                        .seatLabel(label)
                        .status(RideSeat.Status.AVAILABLE)
                        .build());
            }
            rideSeatRepository.saveAll(seats);
            restoredRides++;
            restoredSeats += seats.size();
        }

        if (restoredRides > 0) {
            log.info("Restored {} missing seats across {} existing rides", restoredSeats, restoredRides);
        }
    }
}
