package com.mola.domain.tripBoard.like.repository;

public interface LikesRepositoryCustom {

    boolean existsByMemberIdAndTripPostIdImpl(Long memberId, Long tripPostId);
}
