package com.carpool.app.controller;

import com.carpool.app.dto.BookingDtos.LocationUpdate;
import com.carpool.app.entity.LiveLocation;
import com.carpool.app.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;
    private final SimpMessagingTemplate messagingTemplate;

    // Owner's app sends STOMP frames to /app/rides/{rideId}/location
    @MessageMapping("/rides/{rideId}/location")
    public void updateLocation(@DestinationVariable Long rideId, LocationUpdate update) {
        LiveLocation saved = locationService.updateLocation(rideId, update.getLat(), update.getLng());
        // broadcast to everyone subscribed to /topic/rides/{rideId}/location
        messagingTemplate.convertAndSend("/topic/rides/" + rideId + "/location", saved);
    }

    // REST fallback for clients that poll instead of using WebSocket
    @GetMapping("/api/rides/{rideId}/location")
    public LiveLocation getLocation(@PathVariable Long rideId) {
        return locationService.getLocation(rideId);
    }
}
