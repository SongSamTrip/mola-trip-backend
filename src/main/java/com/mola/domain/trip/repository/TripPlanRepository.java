package com.mola.domain.trip.repository;

import com.mola.domain.trip.entity.TripPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripPlanRepository extends JpaRepository<TripPlan, Long> {
}
