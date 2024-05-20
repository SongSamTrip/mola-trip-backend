package com.mola.domain.trip.controller;

import com.mola.domain.trip.dto.NewTripPlanDto;
import com.mola.domain.trip.dto.TripListHtmlDto;
import com.mola.domain.trip.dto.TripPlanDto;
import com.mola.domain.trip.dto.TripPlanInfoDto;
import com.mola.domain.trip.service.TripPlanService;
import com.mola.global.exception.CustomException;
import com.mola.global.exception.GlobalErrorCode;
import com.mola.global.sse.TripPlanSseRegistry;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class TripPlanController {

    private final TripPlanService tripPlanService;

    private final TripPlanSseRegistry sseRegistry;

    @PostMapping("/trip-plan")
    public ResponseEntity<Long> createTripPlan(@Valid @RequestBody NewTripPlanDto newTripPlanDto, Errors errors) {

        if(errors.hasErrors()){
            throw new CustomException(GlobalErrorCode.MissingRequireData);
        }
        Long tripPlanId = tripPlanService.addTripPlan(newTripPlanDto);
        return ResponseEntity.ok(tripPlanId);
    }

    @GetMapping("/trip-plan/{tripPlanId}")
    public ResponseEntity<TripPlanInfoDto> getTripPlan(@PathVariable("tripPlanId") Long tripPlanId) {
        TripPlanInfoDto tripPlanInfoDto = tripPlanService.getTripList(tripPlanId);
        List<TripPlanDto> tripPlans = tripPlanService.getTripPlans();
        tripPlanInfoDto.setTripPlanDtos(tripPlans);
        return ResponseEntity.ok(tripPlanInfoDto);
    }

    @PostMapping("/trip-plan/{tripCode}")
    public ResponseEntity<Long> addParticipant(@PathVariable("tripCode") String tripCode) {
        Long tripPlanId = tripPlanService.addParticipant(tripCode);
        return ResponseEntity.ok(tripPlanId);
    }


    @PutMapping("/trip-plan/list/{tripId}")
    public ResponseEntity<String> updateTripPlan(@PathVariable("tripId") Long tripId, @RequestBody TripListHtmlDto tripListHtmlDto) {
        TripListHtmlDto newTripListHtmlDto = tripPlanService.updateTripPlanList(tripId, tripListHtmlDto);
        sseRegistry.sendUpdate(tripId, newTripListHtmlDto);
        return ResponseEntity.ok("Trip plan updated successfully");
    }

    @PutMapping("/trip-plan/sub-list/{tripId}")
    public ResponseEntity<String> updateSubList(@PathVariable("tripId") Long tripId, @RequestBody TripListHtmlDto tripListHtmlDto) {
        TripListHtmlDto newTripListHtmlDto = tripPlanService.updateSubPlanList(tripId, tripListHtmlDto);
        sseRegistry.sendUpdate(tripId, newTripListHtmlDto);
        return ResponseEntity.ok("Trip sub plan updated successfully");
    }

    @GetMapping("/trip-plan/lists")
    public ResponseEntity<List<TripPlanDto>> getTripPlanList(){
        List<TripPlanDto> tripPlans = tripPlanService.getTripPlans();
        return ResponseEntity.ok(tripPlans);
    }

    @GetMapping("/stream/trip-plans/{tripId}")
    public SseEmitter streamTripPlan(@PathVariable("tripId") Long tripId) {
        tripPlanService.checkMemberIsInTrip(tripId);
        return sseRegistry.createEmitterForTrip(tripId);
    }


}
