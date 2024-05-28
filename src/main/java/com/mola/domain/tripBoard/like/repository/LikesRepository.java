package com.mola.domain.tripBoard.like.repository;

import com.mola.domain.tripBoard.like.entity.Likes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LikesRepository extends JpaRepository<Likes, Long>, LikesRepositoryCustom {
    @Query("SELECT l FROM Likes l WHERE l.member.id = :memberId AND l.tripPost.id = :tripPostId")
    Likes findByMemberIdAndTripPostId(@Param("memberId") Long memberId, @Param("tripPostId") Long tripPostId);
}