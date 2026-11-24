package com.carpool.app.dto;

import lombok.Data;

import java.time.LocalDateTime;

public class BookingDtos {

    @Data
    public static class BookingRequestResponse {
        private Long id;
        private Long rideId;
        private String seatLabel;
        private String passengerName;
        private String ownerName;
        private String ownerPhone;
        private String status;
        private LocalDateTime requestedAt;
    }

    @Data
    public static class LocationUpdate {
        private Double lat;
        private Double lng;
    }
}
