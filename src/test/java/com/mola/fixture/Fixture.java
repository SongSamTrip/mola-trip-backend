package com.mola.fixture;

import com.mola.domain.member.entity.LoginProvider;
import com.mola.domain.member.entity.Member;
import com.mola.domain.member.entity.MemberRole;
import com.mola.domain.tripBoard.comment.dto.CommentDto;
import com.mola.domain.tripBoard.comment.entity.Comment;
import com.mola.domain.tripBoard.like.entity.Likes;
import com.mola.domain.tripBoard.tripPost.entity.TripPost;
import com.mola.domain.tripBoard.tripPost.entity.TripPostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Fixture {

    public static Member createMember(Long id, String nickname){
        return Member.builder()
                .id(id)
                .nickname(nickname)
                .personalId("test")
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

    public static TripPost createTripPostWithMember(Member member, TripPostStatus status) {
        return TripPost.builder()
                .member(member)
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


    // CommentDto 객체 생성을 위한 기본 더미 데이터 제공 메서드
    public static CommentDto createCommentDto(Long id, Long memberId, String nickname, String content, LocalDateTime createdDate) {
        return new CommentDto(
                id,
                memberId,
                nickname,
                content,
                createdDate
        );
    }

    // 랜덤 데이터를 생성하는 메서드
    public static CommentDto createRandomCommentDto() {
        long id = (long) (Math.random() * 1000); // 임의의 ID
        long memberId = (long) (Math.random() * 1000); // 임의의 Member ID
        String nickname = "User" + id; // 임의의 닉네임
        String content = "This is a test comment."; // 예시 코멘트
        LocalDateTime createdDate = LocalDateTime.now(); // 현재 시간

        return createCommentDto(id, memberId, nickname, content, createdDate);
    }

    public static Page<CommentDto> createCommentDtoPage(int page, int size) {
        List<CommentDto> comments = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            comments.add(createCommentDto(
                    (long) i,
                    (long) (i * 100),
                    "User" + i,
                    "Sample content " + i,
                    LocalDateTime.now().minusDays(i)
            ));
        }

        return new PageImpl<>(comments, PageRequest.of(page, size), comments.size());
    }
}
