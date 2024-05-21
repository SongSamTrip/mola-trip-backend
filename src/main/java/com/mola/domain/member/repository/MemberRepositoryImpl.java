package com.mola.domain.member.repository;

import com.mola.domain.member.dto.MemberTripPostDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static com.mola.domain.member.entity.QMember.member;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<MemberTripPostDto> findMemberTripPostDtoById(Long memberId) {
        return Optional.ofNullable(jpaQueryFactory.select(Projections.constructor(MemberTripPostDto.class,
                member.id,
                member.nickname))
                .from(member)
                .fetchOne());
    }
}
