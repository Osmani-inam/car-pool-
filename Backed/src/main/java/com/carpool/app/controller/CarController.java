package com.carpool.app.controller;

import com.carpool.app.dto.CarDtos.*;
import com.carpool.app.security.CurrentUser;
import com.carpool.app.service.CarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    @PostMapping
    public CarResponse createCar(@Valid @RequestBody CreateCarRequest req) {
        return carService.createCar(CurrentUser.get(), req);
    }

    @GetMapping("/my")
    public List<CarResponse> myCars() {
        return carService.myCars(CurrentUser.get());
    }
}
