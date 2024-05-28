package com.mola.domain.member.service;

import com.mola.domain.member.dto.MemberResponseDto;
import com.mola.domain.member.entity.Member;
import com.mola.domain.member.entity.MemberRole;
import com.mola.domain.member.repository.MemberRepository;
import com.mola.global.exception.CustomException;
import com.mola.global.exception.GlobalErrorCode;
import com.mola.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final SecurityUtil securityUtil;

    private final MemberRepository memberRepository;

    @Value("${admin.secretKey}")
    private String secretKey;

    public Page<MemberResponseDto> getMembersForAdmin(Pageable pageable) {
        return memberRepository.findMembersForAdmin(pageable);
    }

    public void adminDeleteMember(Long memberId) {
        memberRepository.deleteById(memberId);
    }

    public Long requestAdmin(String key) {
        if (!key.equals(secretKey)) {
            throw new CustomException(GlobalErrorCode.AccessDenied);
        }

        Member currentMember = securityUtil.findCurrentMember();
        currentMember.setMemberRole(MemberRole.ADMIN);
        return memberRepository.save(currentMember).getId();
    }
}
