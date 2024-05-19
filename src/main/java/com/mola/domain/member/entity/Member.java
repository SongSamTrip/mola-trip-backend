package com.mola.domain.member.entity;

import com.mola.domain.tripBoard.comment.entity.Comment;
import com.mola.domain.tripBoard.like.entity.Likes;
import com.mola.domain.tripBoard.tripPost.entity.TripPost;
import com.mola.domain.tripFriends.TripFriends;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
public class Member {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    @Column(name = "nickname", length = 100, nullable = false)
    private String nickname;

    @Column(name = "personal_id", nullable = false)
    private String personalId;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @OneToMany(mappedBy = "member",
            fetch = FetchType.LAZY,
            cascade = CascadeType.REMOVE,
            orphanRemoval = true)
    List<TripFriends> tripFriendsList;

    @OneToMany(mappedBy = "member",
            fetch = FetchType.LAZY,
            cascade = CascadeType.REMOVE,
            orphanRemoval = true
    )
    private List<TripPost> tripPosts;

    @Builder.Default
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<Likes> likes = new ArrayList<>();

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<Comment> comments;

    @Column(name = "refresh_token", length = 512)
    private String refreshToken;

    @Column(name = "login_provider")
    private LoginProvider loginProvider;

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void addTripPost(TripPost post){
        this.tripPosts.add(post);
    }

    public void addComment(Comment comment){
        this.comments.add(comment);
    }

    public void addLikes(Likes likes){
        this.likes.add(likes);
    }

    public void deleteTripPost(TripPost tripPost){
        this.tripPosts.remove(tripPost);
    }

    public void deleteComment(Comment comment){
        this.comments.remove(comment);
    }

    public void deleteLikes(Likes likes){
        this.likes.remove(likes);
    }

}
