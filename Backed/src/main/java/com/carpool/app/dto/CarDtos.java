package com.carpool.app.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

public class CarDtos {

    @Data
    public static class CreateCarRequest {
        @NotBlank private String model;
        @NotBlank private String numberPlate;
        @Min(1) private Integer totalSeats;
        // optional custom labels; if omitted, standard S1-S7 positions are generated
        private List<String> seatLabels;
    }

    @Data
    public static class CarResponse {
        private Long id;
        private String model;
        private String numberPlate;
        private Integer totalSeats;
        private List<String> seatLayout;
    }
}
