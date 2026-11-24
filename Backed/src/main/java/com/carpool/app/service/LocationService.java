package com.carpool.app.service;

import com.carpool.app.entity.LiveLocation;
import com.carpool.app.entity.Ride;
import com.carpool.app.repository.LiveLocationRepository;
import com.carpool.app.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LiveLocationRepository liveLocationRepository;
    private final RideRepository rideRepository;

    public LiveLocation updateLocation(Long rideId, Double lat, Double lng) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Ride not found"));

        LiveLocation loc = liveLocationRepository.findById(rideId)
                .orElse(LiveLocation.builder().ride(ride).rideId(rideId).build());
        loc.setLat(lat);
        loc.setLng(lng);
        loc.setUpdatedAt(LocalDateTime.now());
        return liveLocationRepository.save(loc);
    }

    public LiveLocation getLocation(Long rideId) {
        return liveLocationRepository.findById(rideId)
                .orElseThrow(() -> new IllegalArgumentException("No location data for this ride yet"));
    }
}
