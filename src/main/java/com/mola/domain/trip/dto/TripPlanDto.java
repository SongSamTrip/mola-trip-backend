package com.mola.domain.trip.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class TripPlanDto {

    String tripName;
    Long tripId;
    Long totalTripMember;
    String tripImageUrl;
}
