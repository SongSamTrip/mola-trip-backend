package com.mola.domain.tripBoard.entity;

import com.mola.domain.member.entity.Member;
import com.mola.domain.tripBoard.comment.entity.Comment;
import com.mola.domain.tripBoard.like.entity.Likes;
import com.mola.domain.tripBoard.tripPost.entity.TripPost;
import com.mola.domain.tripBoard.tripPost.entity.TripPostStatus;
import com.mola.fixture.Fixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}