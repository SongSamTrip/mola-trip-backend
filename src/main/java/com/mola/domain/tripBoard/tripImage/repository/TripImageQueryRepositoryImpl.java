package com.mola.domain.tripBoard.tripImage.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.mola.domain.tripBoard.tripImage.entity.QTripImage.tripImage;

@RequiredArgsConstructor
@Repository
public class TripImageQueryRepositoryImpl implements TripImageQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public void toPublic(String imageUrl) {
        jpaQueryFactory.update(tripImage)
                .set(tripImage.flag, true)
                .where(tripImage.url.eq(imageUrl))
                .execute();
    }
}
