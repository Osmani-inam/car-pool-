package com.carpool.app.controller;

import com.carpool.app.entity.Rating;
import com.carpool.app.security.CurrentUser;
import com.carpool.app.service.RatingService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @Data
    public static class AddRatingRequest {
        private Long rideId;
        private Long ratedUserId;
        private int stars;
        private String comment;
    }

    @PostMapping("/ratings")
    public Rating addRating(@RequestBody AddRatingRequest req) {
        return ratingService.addRating(CurrentUser.get(), req.getRideId(), req.getRatedUserId(),
                req.getStars(), req.getComment());
    }

    @GetMapping("/users/{id}/ratings")
    public List<Rating> forUser(@PathVariable Long id) {
        return ratingService.forUser(id);
    }
}
