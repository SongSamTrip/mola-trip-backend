package com.mola.global.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.mola.domain.member.entity.Member;
import com.mola.domain.member.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JwtProviderTest {

    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtProvider, "secretKey", "나는배가고프다오늘점심은뭐지");
        ReflectionTestUtils.setField(jwtProvider, "accessTokenExpireTime", 123456L);
        ReflectionTestUtils.setField(jwtProvider, "refreshTokenExpireTime", 123456L);
    }

    @Test
    @DisplayName("AccessToken 발급시 해당 memberId와 올바른 Issuer를 가진다.")
    void createTokenThatHasMemberId() {
        // given
        Long memberId = 1L;
        String accessToken = jwtProvider.createAccessToken(memberId);

        //when
        DecodedJWT jwt = JWT.decode(accessToken);
        Long validMemberId = jwt.getClaim("memberId").asLong();
        String validIssuer = jwt.getIssuer();

        //then
        assertEquals(validMemberId, memberId);
        assertEquals(validIssuer, "MolaTrip");
    }

    @Test
    @DisplayName("RefreshToken 발급시 서로다른 UUID와 올바른 Issuer를 가진다.")
    void createTokenThatHasUuid() {
        //given
        String refreshToken1 = jwtProvider.createRefreshToken();
        String refreshToken2 = jwtProvider.createRefreshToken();

        //when
        DecodedJWT jwt1 = JWT.decode(refreshToken1);
        DecodedJWT jwt2 = JWT.decode(refreshToken2);
        String validIssuer1 = jwt1.getIssuer();
        String validIssuer2 = jwt2.getIssuer();

        //then
        assertNotEquals(refreshToken2, refreshToken1);
        assertEquals(validIssuer1, validIssuer2);
    }

    @Test
    @DisplayName("올바른 토큰이 주어질시 true를 반환")
    void verifyToken_true() {
        //given
        Long memberId = 1L;
        String accessToken = jwtProvider.createAccessToken(memberId);

        //when
        boolean result = jwtProvider.verifyToken(accessToken);

        //then
        assertTrue(result);
    }

    @Test
    @DisplayName("올바른 토큰이 주어질시 false를 반환")
    void verifyToken_false() {
        //given
        Long memberId = 1L;
        String accessToken = jwtProvider.createAccessToken(memberId);
        accessToken = accessToken.substring(0, 3);

        //when
        boolean result = jwtProvider.verifyToken(accessToken);

        //then
        assertFalse(result);
    }

    @Test
    @DisplayName("올바른 memberId와 refreshToken이 주어질시 refreshToken을 저장")
    void saveRefreshToken_success() {
        //given
        Long memberId = 1L;
        String refreshToken = jwtProvider.createRefreshToken();
        Member member = Member.builder().id(memberId).build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        //when
        jwtProvider.updateRefreshToken(memberId, refreshToken);

        //then
        assertEquals(refreshToken, member.getRefreshToken());
        verify(memberRepository, times(1)).save(member);

    }

    @Test
    @DisplayName("올바르지 않은 memberId와 refreshToken이 주어질시 refreshToken을 저장하지 않음")
    void saveRefreshToken_fail() {
        //given
        Long memberId = 123L;
        String refreshToken = jwtProvider.createRefreshToken();
        Member member = Member.builder().id(memberId).build();
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        //when
        assertThrows(IllegalArgumentException.class, () -> jwtProvider.updateRefreshToken(memberId, refreshToken));

        //then
        verify(memberRepository, times(0)).save(member);
    }

    @Test
    @DisplayName("올바른 엑세스토큰엣 올바른 memberId 추출")
    void extractMemberIdFromToken() {
        // given
        Long validMemberId = 1L;
        String accessToken = jwtProvider.createAccessToken(validMemberId);

        //when
        Long tokenMemberId = jwtProvider.extractMemberIdFromToken(accessToken);

        //then
        assertEquals(tokenMemberId, validMemberId);
    }

    @Test
    @DisplayName("옵바른 UserDetails 생성")
    void createUserDetails() {
        //given
        Long invalidMemberId = 1L;
        String role = "ROLE_ADMIN";

        //when
        UserDetails userDetails = jwtProvider.createUserDetails(invalidMemberId, role);

        //then
        assertEquals(userDetails.getUsername(), invalidMemberId.toString());
        assertEquals(userDetails.getPassword(), "");
        boolean hasAdminRole = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        assertTrue(hasAdminRole);
    }
}