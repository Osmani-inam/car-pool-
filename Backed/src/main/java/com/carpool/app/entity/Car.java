package com.carpool.app.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "cars")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 50)
    private String model;

    @Column(name = "number_plate", nullable = false, length = 20)
    private String numberPlate;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    // Stored as JSON text, e.g. ["front","rear-left","rear-middle","rear-right"]
    @Column(name = "seat_layout", columnDefinition = "JSON")
    @Convert(converter = com.carpool.app.entity.converter.StringListJsonConverter.class)
    private List<String> seatLayout;
}
