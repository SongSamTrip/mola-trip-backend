package com.mola.domain.trip.repository;

import com.mola.domain.trip.dto.TripPlanDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.mola.domain.trip.entity.QTripPlan.tripPlan;
import static com.mola.domain.tripFriends.QTripFriends.tripFriends;


@RequiredArgsConstructor
public class TripPlanRepositoryImpl implements TripPlanRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<TripPlanDto> getTripPostDtoByMemberId(Long memberId) {
        return jpaQueryFactory.select(Projections.constructor(TripPlanDto.class,
                        tripPlan.tripName,
                        tripPlan.id,
                        JPAExpressions.select(tripFriends.count())
                                .from(tripFriends)
                                .where(tripFriends.tripPlan.id.eq(tripPlan.id))))
                .from(tripPlan)
                .join(tripPlan.tripFriendsList, tripFriends)
                .where(tripFriends.member.id.eq(memberId))
                .fetch();
    }
}
