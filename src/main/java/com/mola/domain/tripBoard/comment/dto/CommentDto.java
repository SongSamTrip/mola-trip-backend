package com.mola.domain.tripBoard.comment.dto;

import com.mola.domain.member.dto.MemberTripPostDto;
import com.mola.domain.member.entity.Member;
import com.mola.domain.tripBoard.comment.entity.Comment;
import com.mola.domain.tripBoard.tripPost.entity.TripPost;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class CommentDto {

    private Long id;

    private MemberTripPostDto memberTripPostDto;

    private String content;

    private LocalDateTime createdDate;

    public Comment toEntity(String content, Member member, TripPost tripPost){
        return Comment.builder()
                .content(content)
                .member(member)
                .tripPost(tripPost)
                .build();
    }

    public CommentDto (Long commentId, Long id, String nickname, String content, LocalDateTime createdDate){
        this.id = commentId;
        this.memberTripPostDto = new MemberTripPostDto(id, nickname);
        this.content = content;
        this.createdDate = createdDate;
    }
}
