package com.mola.domain.tripBoard.comment.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import static com.mola.domain.tripBoard.comment.entity.QComment.comment;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public boolean isUserAuthorizedForComment(Long commentId, Long memberId) {
        Long fetchOne = jpaQueryFactory.select(comment.member.id)
                .from(comment)
                .where(comment.member.id.eq(commentId))
                .fetchOne();

        return fetchOne == memberId;
    }
}
