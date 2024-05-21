package com.mola.domain.tripBoard.comment.repository;

import com.mola.domain.tripBoard.comment.entity.Comment;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findAllByTripPostId(Long tripPostId, Pageable pageable);

    Optional<Comment> findCommentsByMemberId(Long memberId);

    @Query("SELECT c FROM Comment c WHERE c.member.id = :memberId")
    Optional<Comment> findCommentsByMemberIdJPQL(@Param("memberId") Long memberId);

//    @Query("SELECT l FROM Likes l WHERE l.member.id = :memberId AND l.tripPost.id = :tripPostId")
//    Likes findByMemberIdAndTripPostId(@Param("memberId") Long memberId, @Param("tripPostId") Long tripPostId);
}
