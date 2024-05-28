package com.mola.domain.member.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mola.domain.member.dto.MemberResponseDto;
import com.mola.domain.member.entity.Member;
import com.mola.domain.member.entity.MemberRole;
import com.mola.domain.member.repository.MemberRepository;
import com.mola.global.exception.CustomException;
import com.mola.global.util.SecurityUtil;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private SecurityUtil securityUtil;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(memberService, "secretKey", "testSecretKey");
    }

    @DisplayName("Admin이 회원 목록을 요청할 경우 페이지를 반환한다")
    @Test
    void testGetMembersForAdmin() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<MemberResponseDto> expectedPage = new PageImpl<>(Collections.emptyList());

        // when
        when(memberRepository.findMembersForAdmin(pageable)).thenReturn(expectedPage);

        Page<MemberResponseDto> result = memberService.getMembersForAdmin(pageable);

        // then
        assertEquals(expectedPage, result);
        verify(memberRepository).findMembersForAdmin(pageable);
    }

    @DisplayName("Admin이 회원을 삭제할 경우 성공적으로 삭제 처리한다")
    @Test
    void testAdminDeleteMember() {
        // given
        Long memberId = 1L;
        doNothing().when(memberRepository).deleteById(memberId);

        // when
        memberService.adminDeleteMember(memberId);

        // then
        verify(memberRepository).deleteById(memberId);
    }

    @DisplayName("잘못된 키로 Admin 권한 요청 시 접근 거부 예외를 던진다")
    @Test
    void testRequestAdminWithInvalidKey() {
        // given
        String invalidKey = "invalidKey";
        // when then
        assertThrows(CustomException.class, () -> memberService.requestAdmin(invalidKey));
    }

    @DisplayName("올바른 키로 Admin 권한 요청 시 사용자를 Admin으로 승격시킨다")
    @Test
    void testRequestAdminWithValidKey() {
        // given
        String validKey = "testSecretKey";
        Member currentMember = Member.builder()
                .id(1L)
                .memberRole(MemberRole.USER)
                .nickname("swh")
                .build();

        // when
        when(securityUtil.findCurrentMember()).thenReturn(currentMember);
        when(memberRepository.save(any(Member.class))).thenReturn(currentMember);

        Long result = memberService.requestAdmin(validKey);

        // then
        assertEquals(MemberRole.ADMIN, currentMember.getMemberRole());
        assertEquals(currentMember.getId(), result);
        verify(memberRepository).save(currentMember);
    }

}