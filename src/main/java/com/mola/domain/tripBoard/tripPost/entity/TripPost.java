package com.mola.domain.tripBoard.tripPost.entity;

import com.mola.domain.member.entity.Member;
import com.mola.domain.trip.entity.TripPlan;
import com.mola.domain.tripBoard.comment.entity.Comment;
import com.mola.domain.tripBoard.like.entity.Likes;
import com.mola.domain.tripBoard.tripImage.entity.TripImage;
import com.mola.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
@Setter
@Getter
@Entity
public class TripPost extends BaseEntity {

    @Id @GeneratedValue
    private Long id;

    private String name;

    @ManyToOne
    private Member member;

    @ManyToOne(fetch = FetchType.EAGER)
    private TripPlan tripPlan;

    @Lob
    private String representationImageUrl;

    @Lob
    private String content;

    @Enumerated(EnumType.STRING)
    private TripPostStatus tripPostStatus;

    @Builder.Default
    @OneToMany(mappedBy = "tripPost",
            fetch = FetchType.LAZY,
            cascade = CascadeType.REMOVE,
            orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "tripPost", fetch = FetchType.LAZY)
    private List<TripImage> imageUrl = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "tripPost",
            fetch = FetchType.LAZY,
            cascade = CascadeType.REMOVE,
            orphanRemoval = true)
    private List<Likes> likes = new ArrayList<>();

    private int likeCount;

    @Version
    private Long version = 0L;


    public void deleteRelateEntities(){
        this.comments.forEach(member::deleteComment);
        this.likes.forEach(member::deleteLikes);
        this.member.deleteTripPost(this);
    }

    public void deleteLikes(Likes likes){
        this.likes.remove(likes);
        this.likeCount--;
    }

    public void addComment(Comment comment){
        this.comments.add(comment);
        comment.setTripPost(this);
    }

    public void addLikes(Likes likes){
        this.likes.add(likes);
        this.likeCount++;
    }

    public static TripPost createDraft(Member member){
        TripPost tripPost = new TripPost();
        tripPost.setMember(member);
        tripPost.setTripPostStatus(TripPostStatus.DRAFT);
        return tripPost;
    }

    public void toPublic() {
        this.tripPostStatus = TripPostStatus.PUBLIC;
    }
}

