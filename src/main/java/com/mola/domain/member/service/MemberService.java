package com.mola.domain.member.service;

import com.mola.domain.member.dto.MemberActivityProfile;
import com.mola.domain.member.dto.MemberResponseDto;
import com.mola.domain.member.entity.Member;
import com.mola.domain.member.entity.MemberRole;
import com.mola.domain.member.repository.MemberRepository;
import com.mola.domain.tripBoard.comment.entity.Comment;
import com.mola.domain.tripBoard.comment.repository.CommentRepository;
import com.mola.global.exception.CustomException;
import com.mola.global.exception.GlobalErrorCode;
import com.mola.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final SecurityUtil securityUtil;

    private final CommentRepository commentRepository;

    private final MemberRepository memberRepository;

    @Value("${admin.secretKey}")
    private String secretKey;

    public MemberActivityProfile getMemberActivity() {
        Long memberId = securityUtil.findCurrentMemberId();

        Optional<Comment> comments1 = commentRepository.findCommentsByMemberId(memberId);

        Optional<Comment> comments2 = commentRepository.findCommentsByMemberIdJPQL(memberId);


        return null;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<MemberResponseDto> getMembersForAdmin(Pageable pageable) {
        return memberRepository.findMembersForAdmin(pageable);
    }

    public Long requestAdmin(String key){
        if(!key.equals(secretKey)){
            throw new CustomException(GlobalErrorCode.AccessDenied);
        }

        Member currentMember = securityUtil.findCurrentMember();
        currentMember.setMemberRole(MemberRole.ADMIN);
        return memberRepository.save(currentMember).getId();
    }
}
