package com.mola.domain.tripBoard.repository;

import com.mola.domain.tripBoard.dto.TripPostResponseDto;

public interface TripPostQueryRepository {

    TripPostResponseDto getTripPostResponseDtoById(Long tripPostId);

}
