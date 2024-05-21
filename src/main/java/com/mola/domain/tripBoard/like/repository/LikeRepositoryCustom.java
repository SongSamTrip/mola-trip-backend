package com.mola.domain.tripBoard.like.repository;

public interface LikeRepositoryCustom {

    boolean existsByMemberIdAndTripPostId(Long memberId, Long tripPostId);
}
