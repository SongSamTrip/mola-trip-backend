package com.mola.domain.member.service;

import com.mola.domain.member.dto.MemberActivityProfile;
import com.mola.domain.tripBoard.comment.entity.Comment;
import com.mola.domain.tripBoard.comment.repository.CommentRepository;
import com.mola.global.util.SecurityUtil;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MemberService {
    private final SecurityUtil securityUtil;
    private final CommentRepository commentRepository;
    public MemberActivityProfile getMemberActivity() {
        Long memberId = securityUtil.findCurrentMemberId();

        Optional<Comment> comments1 = commentRepository.findCommentsByMemberId(memberId);

        Optional<Comment> comments2 = commentRepository.findCommentsByMemberIdJPQL(memberId);


        return null;

    }
}
