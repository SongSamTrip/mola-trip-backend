package com.mola.domain.tripBoard.repository;

import com.mola.domain.tripBoard.entity.Likes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LikesRepository extends JpaRepository<Likes, Long> {
    @Query("SELECT COUNT(l) > 0 FROM Likes l WHERE l.member.id = :memberId AND l.tripPost.id = :tripPostId")
    boolean existsByMemberIdAndTripPostId(Long memberId, Long tripPostId);
}