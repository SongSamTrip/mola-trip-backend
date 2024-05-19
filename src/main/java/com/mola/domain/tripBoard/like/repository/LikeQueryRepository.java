package com.mola.domain.tripBoard.like.repository;

public interface LikeQueryRepository {

    boolean existsByMemberIdAndTripPostId(Long memberId, Long tripPostId);
}
