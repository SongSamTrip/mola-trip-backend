package com.mola.domain.member.repository;

import com.mola.domain.member.dto.MemberTripPostDto;

import java.util.Optional;

public interface MemberCustomRepository {

    Optional<MemberTripPostDto> findMemberTripPostDtoById(Long memberId);

}
