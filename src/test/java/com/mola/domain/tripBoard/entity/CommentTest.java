package com.mola.domain.tripBoard.entity;

import com.mola.domain.member.entity.Member;
import com.mola.fixture.Fixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommentTest {

    @DisplayName("댓글이 생성되면 연관된 엔티티에도 적용")
    @Test
    void createLikeWithRelatedEntity() {
        // given
        Member member = Fixture.createMember(1L, "test");
        TripPost tripPost = Fixture.createTripPost(1L, TripPostStatus.PUBLIC);
        Comment comment = Fixture.createComment(1L, "content", member, tripPost);
        assertFalse(member.getLikes().contains(comment));
        assertFalse(tripPost.getLikes().contains(comment));

        // when
        comment.updateRelatedEntities();

        // then
        assertTrue(member.getComments().contains(comment));
        assertTrue(tripPost.getComments().contains(comment));
    }

}