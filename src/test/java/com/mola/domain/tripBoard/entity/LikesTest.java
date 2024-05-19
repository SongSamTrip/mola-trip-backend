package com.mola.domain.tripBoard.entity;

import com.mola.domain.member.entity.Member;
import com.mola.domain.tripBoard.like.entity.Likes;
import com.mola.domain.tripBoard.tripPost.entity.TripPost;
import com.mola.domain.tripBoard.tripPost.entity.TripPostStatus;
import com.mola.fixture.Fixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LikesTest {

    @DisplayName("좋아요가 생성되면 연관된 엔티티에도 적용")
    @Test
    void createLikeWithRelatedEntity() {
        // given
        Member member = Fixture.createMember(1L, "test");
        TripPost tripPost = Fixture.createTripPost(1L, TripPostStatus.PUBLIC);
        Likes likes = Fixture.createLikes(1L, member, tripPost);
        assertFalse(member.getLikes().contains(likes));
        assertFalse(tripPost.getLikes().contains(likes));

        // when
        likes.updateRelatedEntities();

        // then
        assertTrue(member.getLikes().contains(likes));
        assertTrue(tripPost.getLikes().contains(likes));
    }
}