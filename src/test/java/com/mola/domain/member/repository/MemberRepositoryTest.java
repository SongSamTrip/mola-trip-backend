package com.mola.domain.member.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.mola.domain.member.dto.MemberResponseDto;
import com.mola.domain.member.entity.LoginProvider;
import com.mola.domain.member.entity.Member;
import com.mola.domain.member.entity.MemberRole;
import com.mola.fixture.Fixture;
import com.mola.global.config.QueryDslConfig;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;


@Import(QueryDslConfig.class)
@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        Member member1 = Fixture.createSimpleMember("User1", LoginProvider.GITHUB);
        Member member2 = Fixture.createSimpleMember("User2", LoginProvider.NAVER);
        entityManager.persist(member1);
        entityManager.persist(member2);
        entityManager.flush();
    }

    @DisplayName("모든 회원 정보를 조회")
    @Test
    void findMembersForAdmin() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        Page<MemberResponseDto> result = memberRepository.findMembersForAdmin(pageRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);

        List<MemberResponseDto> content = result.getContent();
        assertThat(content.get(0).getNickname()).isEqualTo("User1");
        assertThat(content.get(1).getNickname()).isEqualTo("User2");
        assertThat(content.get(0).getLoginProvider()).isEqualTo(LoginProvider.GITHUB);
        assertThat(content.get(1).getLoginProvider()).isEqualTo(LoginProvider.NAVER);
    }

    @DisplayName("Member의 Role 찾는다")
    @Test
    void findRoleByMemberId() {
        // when
        MemberRole memberRole = memberRepository.findRoleByMemberId(1L);

        // then
        assertThat(memberRole).isNotNull();
        assertThat(memberRole).isEqualTo(MemberRole.USER);
    }
}