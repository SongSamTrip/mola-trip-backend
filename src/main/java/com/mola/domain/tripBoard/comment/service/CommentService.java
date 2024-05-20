package com.mola.domain.tripBoard.comment.service;

import com.mola.domain.member.entity.Member;
import com.mola.domain.member.repository.MemberRepository;
import com.mola.domain.tripBoard.comment.dto.CommentDto;
import com.mola.domain.tripBoard.comment.entity.Comment;
import com.mola.domain.tripBoard.tripPost.entity.TripPost;
import com.mola.domain.tripBoard.comment.repository.CommentRepository;
import com.mola.domain.tripBoard.tripPost.service.TripPostService;
import com.mola.global.exception.CustomException;
import com.mola.global.exception.GlobalErrorCode;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TripPostService tripPostService;
    private final MemberRepository memberRepository;

    private final EntityManager entityManager;

    public List<CommentDto> getAllComments(Long tripPostId, Pageable pageable) {
        if(!tripPostService.existsTripPost(tripPostId)){
            throw new CustomException(GlobalErrorCode.InvalidTripPostIdentifier);
        }

        Page<Comment> allByTripPostId = commentRepository.findAllByTripPostId(tripPostId, pageable);
        List<CommentDto> list = new ArrayList<>();
        allByTripPostId.forEach(comment -> {
            list.add(Comment.toCommentDto(comment));
        });

        return list;
    }

    public Comment findById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.InvalidCommentIdentifier));
    }

    @Transactional
    public CommentDto save(Long tripPostId, String content){
        if(!tripPostService.isPublic(tripPostId)){
            throw new CustomException(GlobalErrorCode.InvalidTripPostIdentifier);
        }

        TripPost tripPost = entityManager.getReference(TripPost.class, tripPostId);

        Long memberId = getAuthenticatedMemberId();

        if(!memberRepository.existsById(memberId)){
            throw new CustomException(GlobalErrorCode.AccessDenied);
        }
        Member member = entityManager.getReference(Member.class, memberId);
        Comment entity = Comment.builder()
                .content(content)
                .member(member)
                .tripPost(tripPost)
                .build();
        return Comment.toCommentDto(commentRepository.save(entity));
    }

    @Transactional
    public CommentDto update(Long tripPostId, Long commentId, CommentDto commentDto){
        TripPost byId = tripPostService.findById(tripPostId);
        if(!byId.isTripPostPublic()){
            throw new CustomException(GlobalErrorCode.AccessDenied);
        }
        Member member = memberRepository.findById(commentDto.getMemberTripPostDto().getId())
                .orElseThrow(() -> new CustomException(GlobalErrorCode.InvalidMemberIdentifierFormat));

        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        if(!name.equals(String.valueOf(member.getId()))){
            throw new CustomException(GlobalErrorCode.AccessDenied);
        }

        Comment comment = findById(commentId);
        comment.setContent(commentDto.getContent());

        return Comment.toCommentDto(commentRepository.save(comment));
    }

    @Transactional
    public void delete(Long tripPostId, Long commentId){
        if(!tripPostService.isPublic(tripPostId)){
            throw new CustomException(GlobalErrorCode.AccessDenied);
        }
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.InvalidMemberIdentifierFormat));

        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        if(!name.equals(String.valueOf(comment.getMember().getId()))){
            throw new CustomException(GlobalErrorCode.AccessDenied);
        }

        commentRepository.deleteById(commentId);
    }


    private Long getAuthenticatedMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CustomException(GlobalErrorCode.AccessDenied);
        }
        return Long.valueOf(authentication.getName());
    }
}
