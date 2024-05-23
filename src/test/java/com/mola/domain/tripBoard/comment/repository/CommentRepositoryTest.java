package com.mola.domain.tripBoard.comment.repository;

import com.mola.domain.member.entity.Member;
import com.mola.domain.tripBoard.comment.entity.Comment;
import com.mola.domain.tripBoard.tripPost.entity.TripPost;
import com.mola.domain.tripBoard.tripPost.entity.TripPostStatus;
import com.mola.fixture.Fixture;
import com.mola.global.config.QueryDslConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import(QueryDslConfig.class)
@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @PersistenceContext
    EntityManager em;

    private Member member;
    private TripPost tripPost;
    private Comment comment;

    @BeforeEach
    void setUp() {
        member = Fixture.createMember(1L, "test");
        tripPost = Fixture.createTripPost(1L, TripPostStatus.PUBLIC);
        tripPost.setVersion(1L);
        comment = Fixture.createComment(null, "test", member, tripPost);

        em.merge(member);
        em.merge(tripPost);
        comment = commentRepository.save(comment);

        em.flush();
        em.clear();
    }


    @DisplayName("댓글을 작성한 회원이라면 true 반환")
    @Test
    void isUserAuthorizedForComment() {
        // when
        boolean userAuthorizedForComment =
                commentRepository.isUserAuthorizedForComment(comment.getId(), member.getId());

        // then
        assertTrue(userAuthorizedForComment);
    }

    @DisplayName("댓글을 작성한 회원이 아니라면 false 반환")
    @Test
    void isUserAuthorizedForComment_false() {
        // given
        Long INVALID_ID = 5L;
        em.merge(Fixture.createComment(INVALID_ID, "test", member, tripPost));
        em.flush();
        em.clear();

        // when
        boolean userAuthorizedForComment =
                commentRepository.isUserAuthorizedForComment(comment.getId(), INVALID_ID);

        // then
        assertFalse(userAuthorizedForComment);
    }
}