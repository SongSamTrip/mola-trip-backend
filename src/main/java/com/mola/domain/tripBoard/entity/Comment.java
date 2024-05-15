package com.mola.domain.tripBoard.entity;

import com.mola.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
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
}
