package com.mola.domain.tripBoard.entity;

import com.mola.domain.member.entity.Member;
import com.mola.domain.tripBoard.dto.TripPostResponseDto;
import com.mola.fixture.Fixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.*;

class TripPostTest {

    private static final Long VALID_ID = 1L;

    @DisplayName("게시글이 삭제될 때 회원의 컬렉션에도 반영")
    @Test
    void deleteTripPostWithMemberCollection() {
        // given
        Member member = Fixture.createMember(VALID_ID, "test");
        TripPost tripPost = Fixture.createTripPost(VALID_ID, TripPostStatus.PUBLIC);
        member.addTripPost(tripPost);
        tripPost.setMember(member);
        LongStream.range(1, 11).forEach(i -> {
            Comment comment = Fixture.createComment(i, "test", member, tripPost);
            Likes likes = Fixture.createLikes(i, member, tripPost);
            member.addComment(comment);
            member.addLikes(likes);
            tripPost.addComment(comment);
            tripPost.addLikes(likes);
        });

        assertEquals(member.getComments().size(), 10);
        assertEquals(member.getLikes().size(), 10);
        assertEquals(tripPost.getComments().size(), 10);
        assertEquals(tripPost.getLikes().size(), 10);

        // when
        tripPost.deleteRelateEntities();

        // then
        assertTrue(member.getComments().isEmpty());
        assertTrue(member.getLikes().isEmpty());
        assertFalse(member.getTripPosts().contains(tripPost));
    }


    @DisplayName("TripResponseDto 로 변환")
    @Test
    void transferToDto() {
        // given
        Member member = Fixture.createMember(1L, "name");
        TripPost tripPost = Fixture.createTripPost(1L, TripPostStatus.PUBLIC);
        tripPost.setName("test");
        tripPost.setContent("content");
        List<Comment> comments = new ArrayList<>();
        List<TripImage> tripImages = new ArrayList<>();
        LongStream.range(1, 11).forEach(i -> {
            comments.add(new Comment(i, "test" + i , member, tripPost));
            tripImages.add(new TripImage(i, "test" + i, tripPost));
        });
        tripPost.setMember(member);
        tripPost.setComments(comments);
        tripPost.setImageUrl(tripImages);

        // when
        TripPostResponseDto dto = TripPost.toTripPostResponseDto(tripPost);

        // then
        assertEquals(tripPost.getId(), dto.getId());
        assertEquals(tripPost.getName(), dto.getName());
        assertEquals(tripPost.getContent(), dto.getContent());
        assertEquals(tripPost.getTripPostStatus().name(), dto.getTripPostStatus().name());
        assertEquals(tripPost.getComments().size(), dto.getCommentCount());
        assertEquals(tripPost.getLikeCount(), dto.getLikeCount());
        assertEquals(tripPost.getImageUrl().stream().map(TripImage::getUrl).collect(Collectors.toList()), dto.getImageList());
        assertEquals(tripPost.getMember().getNickname(), dto.getWriter());
    }
}