package com.mola.domain.tripBoard.entity;

import com.mola.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
@Setter
@Getter
@Entity
public class TripPost {

    @Id @GeneratedValue
    private Long id;
    private String name;
    @ManyToOne
    private Member member;
    @Column(length = 50)
    private String preview;
    private String content;
    private TripPostStatus tripPostStatus;
    @OneToMany(mappedBy = "tripPost", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comment> comments;
    @OneToMany(mappedBy = "tripPost", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<TripImage> imageUrl;
    @OneToMany(mappedBy = "tripPost", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Likes> likes;
    private int likeCount;


    public void deleteRelateEntities(){
        this.comments.forEach(member::deleteComment);
        this.likes.forEach(member::deleteLikes);
        this.member.deleteTripPost(this);
    }

    public void addComment(Comment comment){
        this.comments.add(comment);
    }

    public void addLikes(Likes likes){
        this.likes.add(likes);
        this.likeCount++;
    }

    public boolean isTripPostPublic(){
        return this.tripPostStatus == TripPostStatus.PUBLIC;
    }

    public static TripPost createDraft(){
        TripPost tripPost = new TripPost();
        tripPost.setTripPostStatus(TripPostStatus.DRAFT);
        return tripPost;
    }
}
