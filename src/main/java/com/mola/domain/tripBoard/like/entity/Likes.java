package com.mola.domain.tripBoard.like.entity;

import com.mola.domain.member.entity.Member;
import com.mola.domain.tripBoard.tripPost.entity.TripPost;
import com.mola.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"member", "tripPost"})
@Setter
@Getter
@Entity
public class Likes extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    private TripPost tripPost;

    public void updateRelatedEntities(){
        member.addLikes(this);
        tripPost.addLikes(this);
    }
}
