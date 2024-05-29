package com.mola.domain.tripBoard.tripPost.repository;

import com.mola.domain.tripBoard.comment.dto.CommentDto;
import com.mola.domain.tripBoard.tripPost.dto.TripPostListResponseDto;
import com.mola.domain.tripBoard.tripPost.dto.TripPostResponseDto;
import com.mola.domain.tripBoard.tripPost.entity.TripPostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TripPostRepositoryCustom {

    TripPostResponseDto getTripPostResponseDtoById(Long tripPostId, Long memberId);

    Page<TripPostListResponseDto> getAllTripPostResponseDto(Long memberId, TripPostStatus status, Pageable pageable);

    boolean isTripPostStatusPublic(Long id);

    Page<CommentDto> getCommentsForTripPost(Long tripPostId, Pageable pageable);
}
