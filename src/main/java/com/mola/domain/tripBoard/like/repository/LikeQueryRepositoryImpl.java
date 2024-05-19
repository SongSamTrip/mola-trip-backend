package com.mola.domain.tripBoard.like.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.mola.domain.tripBoard.like.entity.QLikes.likes;

@RequiredArgsConstructor
@Repository
public class LikeQueryRepositoryImpl implements LikeQueryRepository{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public boolean existsByMemberIdAndTripPostId(Long memberId, Long tripPostId) {
        Integer fetchFirst = jpaQueryFactory.selectOne()
                .from(likes)
                .where(likes.member.id.eq(memberId)
                        .and(likes.tripPost.id.eq(tripPostId)))
                .fetchFirst();


        return fetchFirst != null ? true : false;
    }
}
