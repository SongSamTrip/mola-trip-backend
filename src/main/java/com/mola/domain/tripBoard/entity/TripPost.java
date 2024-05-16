package com.mola.domain.tripBoard.entity;

import com.mola.domain.member.entity.Member;
import com.mola.domain.tripBoard.dto.TripPostListResponseDto;
import com.mola.domain.tripBoard.dto.TripPostResponseDto;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @Builder.Default
    @OneToMany(mappedBy = "tripPost", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "tripPost", cascade = CascadeType.REMOVE)
    private List<TripImage> imageUrl = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "tripPost", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Likes> likes = new ArrayList<>();
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

    public void toPublic() {
        this.tripPostStatus = TripPostStatus.DRAFT;
    }


    public static TripPostResponseDto toTripPostResponseDto(TripPost tripPost) {
        return TripPostResponseDto.builder()
                .id(tripPost.getId())
                .name(tripPost.getName())
                .content(tripPost.getContent())
                .tripPostStatus(tripPost.getTripPostStatus())
                .commentCount(tripPost.getComments().size())
                .likeCount(tripPost.getLikeCount())
                .imageList(tripPost.getImageUrl().stream().map(TripImage::getUrl).collect(Collectors.toList()))
                .writer(tripPost.getMember().getNickname())
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

