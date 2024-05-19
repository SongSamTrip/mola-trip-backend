package com.mola.domain.tripBoard.tripPost.entity;

import com.mola.domain.member.dto.MemberTripPostDto;
import com.mola.domain.member.entity.Member;
import com.mola.domain.trip.entity.TripPlan;
import com.mola.domain.tripBoard.comment.entity.Comment;
import com.mola.domain.tripBoard.like.entity.Likes;
import com.mola.domain.tripBoard.tripImage.entity.TripImage;
import com.mola.domain.tripBoard.tripPost.dto.TripPostListResponseDto;
import com.mola.domain.tripBoard.tripPost.dto.TripPostResponseDto;
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
public class TripPost {

    @Id @GeneratedValue
    private Long id;
    private String name;
    @ManyToOne
    private Member member;

    @OneToOne(fetch = FetchType.LAZY)
    private TripPlan tripPlan;

    @Column(length = 50)
    private String preview;

    @Lob
    private String content;

    private TripPostStatus tripPostStatus;

    @Builder.Default
    @OneToMany(mappedBy = "tripPost",
            fetch = FetchType.LAZY,
            cascade = CascadeType.REMOVE,
            orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "tripPost",
            fetch = FetchType.LAZY,
            cascade = CascadeType.REMOVE)
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
    }

    public void addLikes(Likes likes){
        this.likes.add(likes);
        this.likeCount++;
    }

    public boolean isTripPostPublic(){
        return this.tripPostStatus == TripPostStatus.PUBLIC;
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


    public static TripPostResponseDto toTripPostResponseDto(TripPost tripPost, MemberTripPostDto memberTripPostDto) {
        return TripPostResponseDto.builder()
                .id(tripPost.getId())
                .memberId(memberTripPostDto.getId())
                .nickname(memberTripPostDto.getNickname())
                .name(tripPost.getName())
                .content(tripPost.getContent())
                .tripPostStatus(tripPost.getTripPostStatus())
                .commentCount(tripPost.getComments().size())
                .likeCount(tripPost.getLikeCount())
                .build();
    }

    public static TripPostListResponseDto toTripPostListResponseDto(TripPost tripPost) {
        return TripPostListResponseDto.builder()
                .id(tripPost.getId())
                .name(tripPost.getName())
                .preview(tripPost.getContent().substring(0, 50))
                .commentCount(tripPost.getComments().size())
                .likeCount(tripPost.getLikeCount())
                .build();
    }
}

