package com.mola.domain.tripBoard.repository;

import com.mola.domain.member.entity.Member;
import com.mola.domain.member.repository.MemberRepository;
import com.mola.domain.tripBoard.like.entity.Likes;
import com.mola.domain.tripBoard.like.repository.LikesRepository;
import com.mola.domain.tripBoard.tripPost.entity.TripPost;
import com.mola.domain.tripBoard.tripPost.repository.TripPostRepository;
import com.mola.global.config.QueryDslConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Import(QueryDslConfig.class)
@DataJpaTest
class LikesRepositoryTest {

    @Autowired
    LikesRepository likesRepository;

    @Autowired
    TripPostRepository tripPostRepository;

    @Autowired
    MemberRepository memberRepository;


    @Test
    void existsByMemberAndTripPost() {
        Member member = new Member();
        member.setNickname("test");
        member.setPersonalId("1");
        Member savedMember = memberRepository.save(member);

        TripPost tripPost = new TripPost();
        tripPost.setMember(savedMember);
        TripPost savedTripPost = tripPostRepository.save(tripPost);

        Likes likes = new Likes();
        likes.setMember(savedMember);
        likes.setTripPost(savedTripPost);
        likesRepository.save(likes);

        assertTrue(likesRepository.existsByMemberIdAndTripPostIdImpl(savedMember.getId(), savedTripPost.getId()));
    }

}