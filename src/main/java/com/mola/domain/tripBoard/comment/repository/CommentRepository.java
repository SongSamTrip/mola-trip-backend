package com.mola.domain.tripBoard.comment.repository;

import com.mola.domain.tripBoard.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findAllByTripPostId(Long tripPostId, Pageable pageable);
}
