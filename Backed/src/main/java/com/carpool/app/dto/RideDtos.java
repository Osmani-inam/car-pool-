package com.carpool.app.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class RideDtos {

    @Data
    public static class CreateRideRequest {
        @NotNull private Long carId;
        @NotBlank private String origin;
        @NotBlank private String destination;
        private String pickupPoint;
        @NotNull
        @JsonFormat(pattern = "dd/MM/yy")
        private LocalDate rideDate;
        @NotNull private LocalTime rideTime;
        @NotNull private LocalTime endTime;
        @NotNull private BigDecimal pricePerSeat;
    }

    @Data
    public static class RideResponse {
        private Long id;
        private String ownerName;
        private String carModel;
        private String origin;
        private String destination;
        private String pickupPoint;
        @JsonFormat(pattern = "dd/MM/yy")
        private LocalDate rideDate;
        private LocalTime rideTime;
        private LocalTime endTime;
        private BigDecimal pricePerSeat;
        private String status;
        private List<SeatDto> seats;
    }

    // Minimal, public-safe shape for the unauthenticated /rides/upcoming endpoint
    @Data
    public static class UpcomingRidePublicResponse {
        private Long id;
        private String origin;
        private String destination;
        @JsonFormat(pattern = "dd/MM/yy")
        private LocalDate rideDate;
        private LocalTime rideTime;
        private LocalTime endTime;
        private String ownerName;
        private String carModel;
        private BigDecimal pricePerSeat;
        private long seatsLeft;
    }

    @Data
    public static class SeatDto {
        private Long id;
        private String seatLabel;
        private String status;
    }
}
