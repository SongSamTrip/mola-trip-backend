package com.mola.domain.tripBoard.tripPost.repository;

import com.mola.domain.tripBoard.tripPost.dto.TripPostResponseDto;

public interface TripPostQueryRepository {

    TripPostResponseDto getTripPostResponseDtoById(Long tripPostId);

}
