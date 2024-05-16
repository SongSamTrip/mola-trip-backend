package com.mola.domain.trip.controller;

import com.mola.domain.trip.dto.NewTripPlanDto;
import com.mola.domain.trip.service.TripPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class TripPlanController {

    private final TripPlanService tripPlanService;

    @PostMapping("/trip-plan")
    public ResponseEntity<String> createTripPlan(@RequestBody NewTripPlanDto newTripPlanDto) {
        tripPlanService.addTripPlan(newTripPlanDto);
        return ResponseEntity.ok("success");
    }
}
