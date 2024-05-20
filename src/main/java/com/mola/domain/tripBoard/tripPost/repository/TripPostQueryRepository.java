package com.mola.domain.tripBoard.tripPost.repository;

import com.mola.domain.tripBoard.tripPost.dto.TripPostListResponseDto;
import com.mola.domain.tripBoard.tripPost.dto.TripPostResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TripPostQueryRepository {

    TripPostResponseDto getTripPostResponseDtoById(Long tripPostId, Long memberId);

    Page<TripPostListResponseDto> getAllTripPostResponseDto(Pageable pageable);

    boolean isPublic(Long id);

}
