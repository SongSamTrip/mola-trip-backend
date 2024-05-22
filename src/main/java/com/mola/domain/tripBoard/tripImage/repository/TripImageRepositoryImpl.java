package com.mola.domain.tripBoard.tripImage.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import static com.mola.domain.tripBoard.tripImage.entity.QTripImage.tripImage;

@RequiredArgsConstructor
public class TripImageRepositoryImpl implements TripImageRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public void toPublic(String imageUrl) {
        jpaQueryFactory.update(tripImage)
                .set(tripImage.flag, true)
                .where(tripImage.url.eq(imageUrl))
                .execute();
    }
}
