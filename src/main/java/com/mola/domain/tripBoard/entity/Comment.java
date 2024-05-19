package com.mola.domain.tripBoard.entity;

import com.mola.domain.member.dto.MemberTripPostDto;
import com.mola.domain.member.entity.Member;
import com.mola.domain.tripBoard.dto.CommentDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
@Setter
@Getter
@Entity
public class Comment {

    @Id @GeneratedValue
    private Long id;

    private String content;

    @ManyToOne
    private Member member;

    @ManyToOne
    private TripPost tripPost;

    public void updateRelatedEntities(){
        member.addComment(this);
        tripPost.addComment(this);
    }

    public static CommentDto toCommentDto(Comment comment){
        return CommentDto.builder()
                .content(comment.getContent())
                .memberTripPostDto(new MemberTripPostDto(comment.getMember().getId(),
                        comment.getMember().getNickname()))
                .build();
    }
}
