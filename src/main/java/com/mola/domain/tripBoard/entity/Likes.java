package com.mola.domain.tripBoard.entity;

import com.mola.domain.member.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"member", "tripPost"})
@Getter
@Entity
public class Likes {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Member member;

    @ManyToOne
    private TripPost tripPost;

    public void updateRelatedEntities(){
        member.addLikes(this);
        tripPost.addLikes(this);
    }
}
