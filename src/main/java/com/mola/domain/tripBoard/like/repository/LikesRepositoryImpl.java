package com.mola.domain.tripBoard.like.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import static com.mola.domain.tripBoard.like.entity.QLikes.likes;

@RequiredArgsConstructor
public class LikesRepositoryImpl implements LikesRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public boolean existsByMemberIdAndTripPostIdImpl(Long memberId, Long tripPostId) {
        Integer fetchFirst = jpaQueryFactory.selectOne()
                .from(likes)
                .where(likes.member.id.eq(memberId)
                        .and(likes.tripPost.id.eq(tripPostId)))
                .fetchFirst();


        return fetchFirst != null ? true : false;
    }
}
