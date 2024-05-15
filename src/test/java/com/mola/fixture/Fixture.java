package com.mola.fixture;

import com.mola.domain.member.entity.Member;
import com.mola.domain.tripBoard.entity.Comment;
import com.mola.domain.tripBoard.entity.Likes;
import com.mola.domain.tripBoard.entity.TripPost;
import com.mola.domain.tripBoard.entity.TripPostStatus;

import java.util.ArrayList;

public class Fixture {

    public static Member createMember(Long id, String nickname){
        return Member.builder()
                .id(id)
                .nickname(nickname)
                .tripPosts(new ArrayList<>())
                .likes(new ArrayList<>())
                .comments(new ArrayList<>())
                .build();
    }

    public static TripPost createTripPost(Long id, TripPostStatus status) {
        return TripPost.builder()
                .id(id)
                .tripPostStatus(status)
                .comments(new ArrayList<>())
                .likes(new ArrayList<>())
                .build();
    }

    public static Comment createComment(Long id, Member member, TripPost tripPost){
        return new Comment(id, "", member, tripPost);
    }

    public static Likes createLikes(Long id, Member member, TripPost tripPost){
        return new Likes(id, member, tripPost);
    }
}
