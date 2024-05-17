package com.mola.domain.trip.repository;

import com.mola.domain.trip.entity.TripPlan;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripPlanRepository extends JpaRepository<TripPlan, Long> {
    Optional<TripPlan> findByTripCode(String tripCode);
}
