package com.mola.domain.tripBoard.repository;


import com.mola.domain.tripBoard.dto.TripPostResponseDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.mola.domain.member.entity.QMember.member;
import static com.mola.domain.tripBoard.entity.QTripPost.tripPost;

@RequiredArgsConstructor
@Repository
public class TripPostQueryRepositoryImpl implements TripPostQueryRepository{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public TripPostResponseDto getTripPostResponseDtoById(Long tripPostId) {
        return jpaQueryFactory
                .select(Projections.constructor(TripPostResponseDto.class,
                        tripPost.id,
                        member.id,
                        member.nickname,
                        tripPost.name,
                        tripPost.content,
                        tripPost.tripPostStatus,
                        tripPost.comments.size(),
                        tripPost.likes.size()
                ))
                .from(tripPost)
                .join(tripPost.member, member)
                .where(tripPost.id.eq(tripPostId))
                .fetchOne();
    }
}
