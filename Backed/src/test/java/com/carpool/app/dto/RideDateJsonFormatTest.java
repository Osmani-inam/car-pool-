package com.carpool.app.dto;

import com.carpool.app.dto.RideDtos.CreateRideRequest;
import com.carpool.app.dto.RideDtos.RideResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RideDateJsonFormatTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void acceptsAndReturnsDayMonthTwoDigitYear() throws Exception {
        CreateRideRequest request = objectMapper.readValue(
                "{\"rideDate\":\"24/11/26\"}",
                CreateRideRequest.class);
        assertEquals(LocalDate.of(2026, 11, 24), request.getRideDate());

        RideResponse response = new RideResponse();
        response.setRideDate(request.getRideDate());
        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));
        assertEquals("24/11/26", json.get("rideDate").asText());
    }
}
