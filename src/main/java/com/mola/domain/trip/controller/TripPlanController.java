package com.mola.domain.trip.controller;

import com.mola.domain.trip.dto.NewTripPlanDto;
import com.mola.domain.trip.dto.TripListHtmlDto;
import com.mola.domain.trip.service.TripPlanService;
import com.mola.global.exception.CustomException;
import com.mola.global.exception.GlobalErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class TripPlanController {

    private final TripPlanService tripPlanService;

    @PostMapping("/trip-plan")
    public ResponseEntity<String> createTripPlan(@Valid @RequestBody NewTripPlanDto newTripPlanDto, Errors errors) {

        if(errors.hasErrors()){
            throw new CustomException(GlobalErrorCode.MissingRequireData);
        }

        tripPlanService.addTripPlan(newTripPlanDto);
        return ResponseEntity.ok("Add trip plan success");
    }

    @PostMapping("/trip-plan/{tripCode}")
    public ResponseEntity<String> addParticipant(@PathVariable("tripCode") String tripCode) {
        tripPlanService.addParticipant(tripCode);
        return ResponseEntity.ok("Add participant to trip plan success");
    }

    @PutMapping("/trip-plan/list/{tripId}")
    public ResponseEntity<String> updateTripPlan(@PathVariable Long tripId, @RequestBody TripListHtmlDto tripListHtmlDto) {
        tripPlanService.updateTripPlanList(tripId, tripListHtmlDto);
        // TODO : SSE
        return ResponseEntity.ok("Trip plan updated successfully");
    }

    @PutMapping("/trip-plan/sub-list/{tripId}")
    public ResponseEntity<String> updateSubList(@PathVariable("tripId") Long tripId, @RequestBody TripListHtmlDto tripListHtmlDto) {
        tripPlanService.updateSubPlanList(tripId, tripListHtmlDto);
        // TODO : SSE
        return ResponseEntity.ok("Trip sub plan updated successfully");
    }
}
