package com.mola.domain.tripBoard.service;

import com.mola.domain.member.entity.Member;
import com.mola.domain.member.repository.MemberRepository;
import com.mola.domain.tripBoard.tripPost.dto.TripPostDto;
import com.mola.domain.tripBoard.tripPost.dto.TripPostResponseDto;
import com.mola.domain.tripBoard.tripPost.entity.TripPost;
import com.mola.domain.tripBoard.tripPost.entity.TripPostStatus;
import com.mola.domain.tripBoard.tripPost.repository.TripPostRepository;
import com.mola.domain.tripBoard.tripPost.service.TripPostService;
import com.mola.fixture.Fixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class QueryDslTripPostServiceTest {

    @SpyBean
    TripPostService tripPostService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TripPostRepository tripPostRepository;

    Member member;
    TripPost tripPost;

    @BeforeEach
    void setup(){
        member = Fixture.createMember(1L, "test");
        member.setPersonalId("1");
        Member savedMember = memberRepository.save(member);

        tripPost = TripPost.builder()
                .tripPostStatus(TripPostStatus.DRAFT)
                .member(savedMember)
                .version(0L)
                .build();
        tripPost = tripPostRepository.save(tripPost);
    }

    @Test
    void queryDslSave() {
        // given
        TripPostDto tripPostDto = TripPostDto.builder()
                .id(1L)
                .memberId(member.getId())
                .content("test")
                .name("test")
                .build();

        // when
        TripPostResponseDto save = tripPostService.save(tripPostDto);

        // then
        assertThat(save.getName()).isEqualTo("test");
        assertThat(save.getContent()).isEqualTo("test");
        assertThat(save.getNickname()).isEqualTo("test");
    }
}