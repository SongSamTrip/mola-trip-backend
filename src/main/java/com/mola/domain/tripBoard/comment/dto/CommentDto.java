package com.mola.domain.tripBoard.comment.dto;

import com.mola.domain.member.dto.MemberTripPostDto;
import com.mola.domain.member.entity.Member;
import com.mola.domain.tripBoard.comment.entity.Comment;
import com.mola.domain.tripBoard.tripPost.entity.TripPost;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class CommentDto {

    private MemberTripPostDto memberTripPostDto;

    private String content;

    public Comment toEntity(String content, Member member, TripPost tripPost){
        return Comment.builder()
                .content(content)
                .member(member)
                .tripPost(tripPost)
                .build();
    }

    public CommentDto (Long id, String nickname, String content){
        this.memberTripPostDto = new MemberTripPostDto(id, nickname);
        this.content = content;
    }
}
