package com.mola.fixture;

import com.mola.domain.member.entity.LoginProvider;
import com.mola.domain.member.entity.Member;
import com.mola.domain.member.entity.MemberRole;
import com.mola.domain.tripBoard.comment.entity.Comment;
import com.mola.domain.tripBoard.like.entity.Likes;
import com.mola.domain.tripBoard.tripPost.entity.TripPost;
import com.mola.domain.tripBoard.tripPost.entity.TripPostStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

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

    public static Member createSimpleMember(String nickname, LoginProvider loginProvider) {
        return Member.builder()
                .nickname(nickname)
                .personalId("personal_" + nickname)
                .profileImageUrl("http://example.com/image/" + nickname)
                .refreshToken("refreshToken_" + nickname)
                .loginProvider(loginProvider)
                .memberRole(MemberRole.USER)
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

    public static Comment createComment(Long id, String content, Member member, TripPost tripPost){
        return new Comment(id, content, member, tripPost);
    }

    public static Likes createLikes(Long id, Member member, TripPost tripPost){
        return new Likes(id, member, tripPost);
    }

    public static Authentication createAuthentication(String username){
        return new UsernamePasswordAuthenticationToken(username, "ROLE_USER");
    }
}
