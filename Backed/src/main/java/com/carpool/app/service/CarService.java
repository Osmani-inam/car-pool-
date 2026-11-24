package com.carpool.app.service;

import com.carpool.app.dto.CarDtos.*;
import com.carpool.app.entity.Car;
import com.carpool.app.entity.User;
import com.carpool.app.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;

    public CarResponse createCar(User owner, CreateCarRequest req) {
        List<String> layout = req.getSeatLabels();
        if (layout == null || layout.isEmpty()) {
            layout = defaultSeatLabels(req.getTotalSeats());
        }

        Car car = Car.builder()
                .owner(owner)
                .model(req.getModel())
                .numberPlate(req.getNumberPlate())
                .totalSeats(req.getTotalSeats())
                .seatLayout(layout)
                .build();
        car = carRepository.save(car);
        return toResponse(car);
    }

    private List<String> defaultSeatLabels(int totalSeats) {
        List<String> positions = new ArrayList<>(List.of(
                "S1 - Driver side seat",
                "S2 - Back right window",
                "S3 - Back middle",
                "S4 - Back left window",
                "S5 - Left back row",
                "S6 - Middle back row",
                "S7 - Right back row"
        ));

        for (int seatNumber = positions.size() + 1; seatNumber <= totalSeats; seatNumber++) {
            positions.add("S" + seatNumber + " - Extra row seat");
        }
        return new ArrayList<>(positions.subList(0, totalSeats));
    }
    public List<CarResponse> myCars(User owner) {
        return carRepository.findByOwnerId(owner.getId()).stream()
                .peek(this::upgradeLegacySeatLabels)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private void upgradeLegacySeatLabels(Car car) {
        List<String> labels = car.getSeatLayout();
        if (labels != null && !labels.isEmpty() && labels.get(0).startsWith("seat-")) {
            car.setSeatLayout(defaultSeatLabels(car.getTotalSeats()));
            carRepository.save(car);
        }
    }

    private CarResponse toResponse(Car car) {
        CarResponse r = new CarResponse();
        r.setId(car.getId());
        r.setModel(car.getModel());
        r.setNumberPlate(car.getNumberPlate());
        r.setTotalSeats(car.getTotalSeats());
        r.setSeatLayout(car.getSeatLayout());
        return r;
    }
}
