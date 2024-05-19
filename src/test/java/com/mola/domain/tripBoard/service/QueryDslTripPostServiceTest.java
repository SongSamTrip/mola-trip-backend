package com.mola.domain.tripBoard.service;

import com.mola.domain.member.entity.Member;
import com.mola.domain.member.repository.MemberRepository;
import com.mola.domain.tripBoard.tripPost.repository.TripPostRepository;
import com.mola.domain.tripBoard.tripPost.service.TripPostService;
import com.mola.fixture.Fixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.Map;

import static org.mockito.Mockito.doReturn;

@SpringBootTest
class QueryDslTripPostServiceTest {

    @SpyBean
    TripPostService tripPostService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TripPostRepository tripPostRepository;

    Member member;

    @BeforeEach
    void setup(){
        member = Fixture.createMember(1L, "test");
        member.setPersonalId("1");
        memberRepository.save(member);
    }

    @Test
    void queryDslSave() {
        doReturn(1L).when(tripPostService).getMemberId();
        Map<String, Long> draftTripPost = tripPostService.createDraftTripPost();

        tripPostRepository.getTripPostResponseDtoById(draftTripPost.get("tempPostId"));

    }

}