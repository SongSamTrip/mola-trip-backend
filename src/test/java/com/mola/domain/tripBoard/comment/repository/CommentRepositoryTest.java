package com.mola.domain.tripBoard.comment.repository;

import com.mola.domain.member.entity.Member;
import com.mola.domain.tripBoard.comment.entity.Comment;
import com.mola.domain.tripBoard.tripPost.entity.TripPost;
import com.mola.domain.tripBoard.tripPost.entity.TripPostStatus;
import com.mola.fixture.Fixture;
import com.mola.global.config.QueryDslConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertFalse;

@Import(QueryDslConfig.class)
@DataJpaTest
@Transactional
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    TestEntityManager em;

    private Member member;
    private TripPost tripPost;
    private Comment comment;

    @BeforeEach
    void setUp() {
        member = Fixture.createMember(null, "test");
        tripPost = Fixture.createTripPost(null, TripPostStatus.PUBLIC);
        tripPost.setVersion(1L);
        comment = Fixture.createComment(null, "test", member, tripPost);

        em.persist(member);
        em.persist(tripPost);

        comment = commentRepository.save(comment);
    }

    @AfterEach
    void tearDown() {
        em.flush();
        em.clear();
        em.getEntityManager().createQuery("DELETE FROM Comment").executeUpdate();
        em.getEntityManager().createQuery("DELETE FROM TripPost").executeUpdate();
        em.getEntityManager().createQuery("DELETE FROM Member").executeUpdate();
    }

    @DisplayName("댓글을 작성한 회원이 아니라면 false 반환")
    @Test
    void isUserAuthorizedForComment_returnFalse() {
        // given
        Long INVALID_ID = 1234L;

        // when
        boolean userAuthorizedForComment =
                commentRepository.isUserAuthorizedForComment(comment.getId(), INVALID_ID);

        // then
        assertFalse(userAuthorizedForComment);
    }
}