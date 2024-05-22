package com.mola.domain.member.repository;

import com.mola.domain.member.dto.MemberResponseDto;
import com.mola.domain.member.dto.MemberTripPostDto;
import com.mola.domain.member.entity.MemberRole;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
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

    @Override
    public Page<MemberResponseDto> findMembersForAdmin(Pageable pageable) {
        List<MemberResponseDto> fetch = jpaQueryFactory.select(Projections.constructor(MemberResponseDto.class,
                        member.id,
                        member.nickname,
                        member.personalId,
                        member.profileImageUrl,
                        member.loginProvider,
                        member.memberRole,
                        member.createdDate,
                        member.lastModifiedDate))
                .from(member)
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        long total = jpaQueryFactory
                .selectFrom(member)
                .fetchCount();

        return new PageImpl<>(fetch, pageable, total);
    }

    @Override
    public MemberRole findRoleByMemberId(Long memberId) {
        return jpaQueryFactory
                .select(member.memberRole)
                .from(member)
                .where(member.id.eq(memberId))
                .fetchOne();
    }
}
