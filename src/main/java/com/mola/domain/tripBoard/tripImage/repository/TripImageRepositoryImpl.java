package com.mola.domain.tripBoard.tripImage.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import static com.mola.domain.tripBoard.tripImage.entity.QTripImage.tripImage;

@RequiredArgsConstructor
public class TripImageRepositoryImpl implements TripImageRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;
}
