package com.carpool.app.repository;

import com.carpool.app.entity.LiveLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LiveLocationRepository extends JpaRepository<LiveLocation, Long> {
}
