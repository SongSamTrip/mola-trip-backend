package com.mola.domain.trip.repository;

import com.mola.domain.trip.entity.TripPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TripPlanRepository extends JpaRepository<TripPlan, Long>, TripPlanRepositoryCustom {
    Optional<TripPlan> findByTripCode(String tripCode);
}
