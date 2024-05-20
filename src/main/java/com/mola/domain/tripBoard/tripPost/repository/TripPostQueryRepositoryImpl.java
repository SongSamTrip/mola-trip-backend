package com.mola.domain.tripBoard.tripPost.repository;


import com.mola.domain.tripBoard.comment.dto.CommentDto;
import com.mola.domain.tripBoard.tripPost.dto.TripPostListResponseDto;
import com.mola.domain.tripBoard.tripPost.dto.TripPostResponseDto;
import com.mola.domain.tripBoard.tripPost.entity.TripPostStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.mola.domain.member.entity.QMember.member;
import static com.mola.domain.tripBoard.comment.entity.QComment.comment;
import static com.mola.domain.tripBoard.like.entity.QLikes.likes;
import static com.mola.domain.tripBoard.tripPost.entity.QTripPost.tripPost;

@RequiredArgsConstructor
@Repository
public class TripPostQueryRepositoryImpl implements TripPostQueryRepository{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public TripPostResponseDto getTripPostResponseDtoById(Long tripPostId, Long memberId) {
        BooleanExpression isLike = JPAExpressions
                .selectOne()
                .from(likes)
                .where(likes.tripPost.id.eq(tripPostId)
                        .and(likes.member.id.eq(memberId)))
                .exists();

        TripPostResponseDto tripPostDto = jpaQueryFactory
                .select(Projections.constructor(TripPostResponseDto.class,
                        tripPost.id,
                        member.id,
                        member.nickname,
                        tripPost.name,
                        tripPost.content,
                        tripPost.tripPostStatus,
                        tripPost.likeCount,
                        isLike.as("isLike")
                ))
                .from(tripPost)
                .join(tripPost.member, member)
                .where(tripPost.id.eq(tripPostId))
                .fetchOne();

        if (tripPostDto != null) {
            List<CommentDto> comments = getCommentsForTripPost(tripPostId);
            tripPostDto.setCommentDtos(comments);
        }

        return tripPostDto;
    }


    public List<CommentDto> getCommentsForTripPost(Long tripPostId) {
        return jpaQueryFactory
                .select(Projections.constructor(CommentDto.class,
                        comment.id,
                        member.id,
                        member.nickname,
                        comment.content
                ))
                .from(comment)
                .join(comment.member, member)
                .where(comment.tripPost.id.eq(tripPostId))
                .limit(10)
                .fetch();
    }

    @Override
    public boolean isPublic(Long id) {
        TripPostStatus tripPostStatus = jpaQueryFactory.select(tripPost.tripPostStatus)
                .from(tripPost)
                .where(tripPost.id.eq(id))
                .fetchOne();

        return tripPostStatus == TripPostStatus.PUBLIC;
    }

    @Override
    public Page<TripPostListResponseDto> getAllTripPostResponseDto(Pageable pageable) {
        List<TripPostListResponseDto> content = jpaQueryFactory.select(Projections.constructor(TripPostListResponseDto.class,
                        tripPost.id,
                        tripPost.name,
                        tripPost.comments.size(),
                        tripPost.likeCount))
                .from(tripPost)
                .where(tripPost.tripPostStatus.eq(TripPostStatus.PUBLIC))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = jpaQueryFactory.selectFrom(tripPost)
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
    }
}
