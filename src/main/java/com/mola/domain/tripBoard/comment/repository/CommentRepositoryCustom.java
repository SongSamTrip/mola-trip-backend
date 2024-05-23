package com.mola.domain.tripBoard.comment.repository;

public interface CommentRepositoryCustom {

    boolean isUserAuthorizedForComment(Long commentId, Long memberId);
}
