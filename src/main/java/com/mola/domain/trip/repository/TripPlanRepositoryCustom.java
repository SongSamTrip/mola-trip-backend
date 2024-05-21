package com.mola.domain.trip.repository;

import com.mola.domain.trip.dto.TripPlanDto;

import java.util.List;

public interface TripPlanRepositoryCustom {

    List<TripPlanDto> getTripPostDtoByMemberId(Long memberId);

}
