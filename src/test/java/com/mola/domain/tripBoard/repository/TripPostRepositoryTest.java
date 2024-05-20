package com.mola.domain.tripBoard.repository;

import com.mola.domain.member.entity.Member;
import com.mola.domain.member.repository.MemberRepository;
import com.mola.domain.tripBoard.comment.entity.Comment;
import com.mola.domain.tripBoard.comment.repository.CommentRepository;
import com.mola.domain.tripBoard.tripPost.dto.TripPostListResponseDto;
import com.mola.domain.tripBoard.tripPost.entity.TripPost;
import com.mola.domain.tripBoard.tripPost.repository.TripPostRepository;
import com.mola.global.config.QueryDslConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import(QueryDslConfig.class)
@DataJpaTest
class TripPostRepositoryTest {

    @Autowired
    TripPostRepository tripPostRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    CommentRepository commentRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    void findByIdWithOptimisticLock() {
        TripPost tripPost = new TripPost();
        TripPost save = tripPostRepository.save(tripPost);

        TripPost byIdWithOptimisticLock = tripPostRepository.findByIdWithOptimisticLock(save.getId());

        assertNotNull(byIdWithOptimisticLock);
    }

    @Test
    void findAll() {
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);

        Member member = new Member();
        member.setNickname("test");
        member.setPersonalId("1");

        Member savedMember = memberRepository.save(member);

        IntStream.range(1, 10).forEach(i -> {
            TripPost tripPost = new TripPost();
            TripPost save = tripPostRepository.save(tripPost);

            IntStream.range(1, 10).forEach(j -> {
                Comment comment = new Comment();
                comment.setMember(savedMember);
                comment.setContent("test");
                comment.setTripPost(save);
                Comment savedComment = commentRepository.save(comment);
                save.getComments().add(savedComment);
            });
        });

        em.flush();
        em.clear();

        System.out.println("====================================================");

        Page<TripPostListResponseDto> all = tripPostRepository.getAllTripPostResponseDto(pageable);
        all.stream().forEach(trip ->
                System.out.println(trip.getCommentCount()));
    }
}