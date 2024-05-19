package com.mola.domain.tripBoard.repository;

import com.mola.domain.member.entity.Member;
import com.mola.domain.member.repository.MemberRepository;
import com.mola.domain.tripBoard.like.entity.Likes;
import com.mola.domain.tripBoard.like.repository.LikesRepository;
import com.mola.domain.tripBoard.tripPost.entity.TripPost;
import com.mola.domain.tripBoard.tripPost.repository.TripPostRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
        TripPost tripPost = new TripPost();
        TripPost save = tripPostRepository.save(tripPost);

        Member member = new Member();
        member.setNickname("test");
        member.setPersonalId("1");
        Member savedMember = memberRepository.save(member);

        Likes likes = new Likes();
        likes.setMember(savedMember);
        likes.setTripPost(save);

        likesRepository.save(likes);

        assertTrue(likesRepository.existsByMemberIdAndTripPostId(savedMember.getId(), tripPost.getId()));
    }

}