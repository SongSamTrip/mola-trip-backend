package com.mola.domain.member.repository;

import com.mola.domain.member.dto.MemberResponseDto;
import com.mola.domain.member.dto.MemberTripPostDto;
import com.mola.domain.member.entity.MemberRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface MemberRepositoryCustom {

    Optional<MemberTripPostDto> findMemberTripPostDtoById(Long memberId);

    Page<MemberResponseDto> findMembersForAdmin(Pageable pageable);

    MemberRole findRoleByMemberId(Long memberId);

}
