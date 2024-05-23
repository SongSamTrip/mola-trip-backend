package com.mola.domain.tripBoard.comment.repository;

import com.mola.domain.tripBoard.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {

    Optional<Comment> findCommentsByMemberId(Long memberId);

    @Query("SELECT c FROM Comment c WHERE c.member.id = :memberId")
    Optional<Comment> findCommentsByMemberIdJPQL(@Param("memberId") Long memberId);


}
