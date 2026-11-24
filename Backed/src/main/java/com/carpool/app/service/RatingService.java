package com.carpool.app.service;

import com.carpool.app.entity.Rating;
import com.carpool.app.entity.Ride;
import com.carpool.app.entity.User;
import com.carpool.app.repository.RatingRepository;
import com.carpool.app.repository.RideRepository;
import com.carpool.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final RideRepository rideRepository;
    private final UserRepository userRepository;

    public Rating addRating(User ratedBy, Long rideId, Long ratedUserId, int stars, String comment) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Ride not found"));
        if (ride.getStatus() != Ride.Status.COMPLETED) {
            throw new IllegalStateException("Can only rate after the ride is completed");
        }
        User ratedUser = userRepository.findById(ratedUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Rating rating = Rating.builder()
                .ride(ride)
                .ratedBy(ratedBy)
                .ratedUser(ratedUser)
                .stars(stars)
                .comment(comment)
                .build();
        rating = ratingRepository.save(rating);

        // recompute average
        List<Rating> all = ratingRepository.findByRatedUserId(ratedUserId);
        double avg = all.stream().mapToInt(Rating::getStars).average().orElse(0.0);
        ratedUser.setAvgRating(avg);
        userRepository.save(ratedUser);

        return rating;
    }

    public List<Rating> forUser(Long userId) {
        return ratingRepository.findByRatedUserId(userId);
    }
}
